package com.fitness.backend.food;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TacoFoodSeedRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TacoFoodSeedRunner.class);
    private static final DataFormatter DATA_FORMATTER = new DataFormatter(Locale.US);
    private static final int MAX_NAME_LENGTH = 255;
    private static final int MAX_SOURCE_CODE_LENGTH = 50;

    private final FoodRepository foodRepository;
    private final ResourceLoader resourceLoader;
    private final boolean enabled;
    private final String resourcePath;
    private final String source;
    private final boolean skipIfExists;

    public TacoFoodSeedRunner(
        FoodRepository foodRepository,
        ResourceLoader resourceLoader,
        @Value("${app.food.seed.enabled:true}") boolean enabled,
        @Value("${app.food.seed.resource:classpath:seed/alimentos.xlsx}") String resourcePath,
        @Value("${app.food.seed.source:TACO}") String source,
        @Value("${app.food.seed.skip-if-exists:true}") boolean skipIfExists
    ) {
        this.foodRepository = foodRepository;
        this.resourceLoader = resourceLoader;
        this.enabled = enabled;
        this.resourcePath = resourcePath;
        this.source = source;
        this.skipIfExists = skipIfExists;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }

        if (skipIfExists && foodRepository.countBySourceIgnoreCase(source) > 0) {
            log.info("Food seed skipped: source '{}' already has data", source);
            return;
        }

        Resource resource = resourceLoader.getResource(resourcePath);
        if (!resource.exists()) {
            log.warn("Food seed skipped: resource '{}' not found", resourcePath);
            return;
        }

        try (InputStream inputStream = resource.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            List<SeedFoodRow> rows = parseRows(sheet);
            ImportSummary summary = upsertFoods(rows);
            log.info(
                "Food seed completed from {}: inserted={}, updated={}, skipped={}, rejected={}",
                resourcePath,
                summary.inserted(),
                summary.updated(),
                summary.skipped(),
                summary.rejected()
            );
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to import foods from seed resource", ex);
        }
    }

    private ImportSummary upsertFoods(List<SeedFoodRow> rows) {
        List<Food> existingFoods = foodRepository.findBySourceIgnoreCase(source);
        Map<String, Food> bySourceCode = new HashMap<>();
        Map<String, Food> byNormalizedName = new HashMap<>();

        for (Food existing : existingFoods) {
            if (existing.getSourceCode() != null && !existing.getSourceCode().isBlank()) {
                bySourceCode.put(existing.getSourceCode().trim(), existing);
            }
            byNormalizedName.put(normalizeText(existing.getName()), existing);
        }

        Instant now = Instant.now();
        List<Food> toSave = new ArrayList<>();
        Set<Food> touched = new HashSet<>();
        int inserted = 0;
        int updated = 0;
        int skipped = 0;

        for (SeedFoodRow row : rows) {
            String normalizedName = normalizeText(row.name());
            if (normalizedName.isBlank()) {
                skipped++;
                continue;
            }

            Food target = null;
            if (row.sourceCode() != null && !row.sourceCode().isBlank()) {
                target = bySourceCode.get(row.sourceCode().trim());
            }
            if (target == null) {
                target = byNormalizedName.get(normalizedName);
            }

            if (target == null) {
                target = new Food();
                target.setCreatedAt(now);
                inserted++;
            } else {
                updated++;
            }

            target.setSource(source);
            target.setSourceCode(truncate(row.sourceCode(), MAX_SOURCE_CODE_LENGTH));
            target.setName(truncate(row.name(), MAX_NAME_LENGTH));
            target.setKcalPer100g(row.kcalPer100g());
            target.setCarbsPer100g(row.carbsPer100g());
            target.setProteinPer100g(row.proteinPer100g());
            target.setFatPer100g(row.fatPer100g());
            target.setActive(true);
            target.setUpdatedAt(now);

            if (!touched.contains(target)) {
                toSave.add(target);
                touched.add(target);
            }

            if (row.sourceCode() != null && !row.sourceCode().isBlank()) {
                bySourceCode.put(row.sourceCode().trim(), target);
            }
            byNormalizedName.put(normalizedName, target);
        }

        if (!toSave.isEmpty()) {
            foodRepository.saveAll(toSave);
        }

        int rejected = 0;
        return new ImportSummary(inserted, updated, skipped, rejected);
    }

    private List<SeedFoodRow> parseRows(Sheet sheet) {
        Row headerRow = findHeaderRow(sheet);
        if (headerRow == null) {
            throw new IllegalStateException("Header row not found in seed spreadsheet");
        }

        HeaderIndexes indexes = resolveIndexes(headerRow);
        if (indexes.name() < 0 || indexes.kcal() < 0) {
            throw new IllegalStateException("Required TACO columns were not found");
        }

        List<SeedFoodRow> rows = new ArrayList<>();
        int start = headerRow.getRowNum() + 1;
        int last = sheet.getLastRowNum();

        for (int rowIndex = start; rowIndex <= last; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }

            String name = readText(row.getCell(indexes.name()));
            if (name.isBlank()) {
                continue;
            }

            String sourceCode = indexes.code() >= 0 ? readText(row.getCell(indexes.code())) : null;
            if (sourceCode != null && sourceCode.isBlank()) {
                sourceCode = null;
            }

            SeedFoodRow foodRow = new SeedFoodRow(
                sourceCode,
                name,
                readDecimal(row.getCell(indexes.kcal())),
                readDecimal(row.getCell(indexes.carbs())),
                readDecimal(row.getCell(indexes.protein())),
                readDecimal(row.getCell(indexes.fat()))
            );
            rows.add(foodRow);
        }

        return rows;
    }

    private Row findHeaderRow(Sheet sheet) {
        int limit = Math.min(sheet.getLastRowNum(), 30);
        for (int i = 0; i <= limit; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            HeaderIndexes indexes = resolveIndexes(row);
            if (indexes.name() >= 0 && indexes.kcal() >= 0) {
                return row;
            }
        }
        return null;
    }

    private HeaderIndexes resolveIndexes(Row row) {
        int code = -1;
        int name = -1;
        int kcal = -1;
        int carbs = -1;
        int protein = -1;
        int fat = -1;

        short first = row.getFirstCellNum();
        if (first < 0) {
            return new HeaderIndexes(code, name, kcal, carbs, protein, fat);
        }

        for (int i = first; i < row.getLastCellNum(); i++) {
            String normalized = normalizeText(readText(row.getCell(i)));
            if (normalized.isBlank()) {
                continue;
            }

            if (code < 0 && matchesAny(normalized, "numero", "codigo", "id")) {
                code = i;
                continue;
            }
            if (name < 0 && matchesAny(normalized, "descricao", "alimento", "nome")) {
                name = i;
                continue;
            }
            if (kcal < 0 && matchesAny(normalized, "energiakcal", "kcal", "valorenergetico")) {
                kcal = i;
                continue;
            }
            if (carbs < 0 && matchesAny(normalized, "carboidrato", "carboidratos", "carboidratototal")) {
                carbs = i;
                continue;
            }
            if (protein < 0 && matchesAny(normalized, "proteina", "proteinas")) {
                protein = i;
                continue;
            }
            if (fat < 0 && matchesAny(normalized, "lipideos", "lipideo", "gorduratotal")) {
                fat = i;
            }
        }

        if (name < 0 && row.getLastCellNum() > 1) {
            name = 1;
        }
        if (kcal < 0 && row.getLastCellNum() > 3) {
            kcal = 3;
        }
        if (protein < 0 && row.getLastCellNum() > 5) {
            protein = 5;
        }
        if (fat < 0 && row.getLastCellNum() > 6) {
            fat = 6;
        }
        if (carbs < 0 && row.getLastCellNum() > 8) {
            carbs = 8;
        }
        if (code < 0 && row.getLastCellNum() > 0) {
            code = 0;
        }

        return new HeaderIndexes(code, name, kcal, carbs, protein, fat);
    }

    private boolean matchesAny(String normalized, String... keys) {
        for (String key : keys) {
            if (normalized.contains(key)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "");

        return normalized.trim();
    }

    private String readText(Cell cell) {
        if (cell == null) {
            return "";
        }
        return DATA_FORMATTER.formatCellValue(cell).trim();
    }

    private BigDecimal readDecimal(Cell cell) {
        String raw = readText(cell);
        if (raw.isBlank()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        String normalized = normalizeToken(raw);
        if (normalized.isBlank() || "tr".equalsIgnoreCase(normalized)) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        String number = normalized
            .replace(",", ".")
            .replaceAll("[^0-9.-]", "");

        if (number.isBlank() || "-".equals(number) || ".".equals(number)) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        try {
            return new BigDecimal(number).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
    }

    private String normalizeToken(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }
        return trimmed.substring(0, maxLength);
    }

    record SeedFoodRow(
        String sourceCode,
        String name,
        BigDecimal kcalPer100g,
        BigDecimal carbsPer100g,
        BigDecimal proteinPer100g,
        BigDecimal fatPer100g
    ) {}

    record HeaderIndexes(int code, int name, int kcal, int carbs, int protein, int fat) {}

    record ImportSummary(int inserted, int updated, int skipped, int rejected) {}
}
