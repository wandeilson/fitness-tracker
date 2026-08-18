package com.fitness.backend.food;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ResourceLoader;

@ExtendWith(MockitoExtension.class)
class TacoFoodSeedRunnerTest {

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private ResourceLoader resourceLoader;

    @Test
    void runShouldSkipWhenSeedIsDisabled() throws Exception {
        TacoFoodSeedRunner runner = new TacoFoodSeedRunner(
            foodRepository,
            resourceLoader,
            false,
            "classpath:seed/alimentos.xlsx",
            "TACO",
            true
        );

        runner.run(new DefaultApplicationArguments(new String[0]));

        verify(foodRepository, never()).saveAll(anyList());
    }

    @Test
    void runShouldSkipWhenSourceAlreadyHasDataAndSkipIsEnabled() throws Exception {
        when(foodRepository.countBySourceIgnoreCase("TACO")).thenReturn(5L);

        TacoFoodSeedRunner runner = new TacoFoodSeedRunner(
            foodRepository,
            resourceLoader,
            true,
            "classpath:seed/alimentos.xlsx",
            "TACO",
            true
        );

        runner.run(new DefaultApplicationArguments(new String[0]));

        verify(foodRepository, never()).saveAll(anyList());
    }

    @Test
    void runShouldInsertFoodsFromClasspathWorkbook() throws Exception {
        byte[] workbookBytes = createWorkbookBytes(List.<String[]>of(
            new String[] { "1", "Arroz cozido", "128", "2,5", "0,3", "28" },
            new String[] { "2", "Feijao cozido", "76", "4,8", "0,5", "13,6" }
        ));
        when(foodRepository.countBySourceIgnoreCase("TACO")).thenReturn(0L);
        when(foodRepository.findBySourceIgnoreCase("TACO")).thenReturn(List.of());
        when(resourceLoader.getResource("classpath:seed/alimentos.xlsx")).thenReturn(new ByteArrayResource(workbookBytes));

        TacoFoodSeedRunner runner = new TacoFoodSeedRunner(
            foodRepository,
            resourceLoader,
            true,
            "classpath:seed/alimentos.xlsx",
            "TACO",
            true
        );

        runner.run(new DefaultApplicationArguments(new String[0]));

        ArgumentCaptor<List<Food>> captor = ArgumentCaptor.forClass(List.class);
        verify(foodRepository).saveAll(captor.capture());
        List<Food> saved = captor.getValue();

        assertEquals(2, saved.size());
        assertEquals("Arroz cozido", saved.getFirst().getName());
        assertEquals(new BigDecimal("128.00"), saved.getFirst().getKcalPer100g());
        assertEquals(new BigDecimal("28.00"), saved.getFirst().getCarbsPer100g());
    }

    @Test
    void runShouldUpdateExistingFoodWhenSourceCodeMatches() throws Exception {
        byte[] workbookBytes = createWorkbookBytes(List.<String[]>of(
            new String[] { "10", "Iogurte natural", "63", "3,9", "3,5", "4,7" }
        ));

        Food existing = new Food();
        existing.setSource("TACO");
        existing.setSourceCode("10");
        existing.setName("Iogurte");
        existing.setKcalPer100g(new BigDecimal("50.00"));
        existing.setCarbsPer100g(new BigDecimal("1.00"));
        existing.setProteinPer100g(new BigDecimal("1.00"));
        existing.setFatPer100g(new BigDecimal("1.00"));
        existing.setActive(true);

        when(foodRepository.findBySourceIgnoreCase("TACO")).thenReturn(List.of(existing));
        when(resourceLoader.getResource("classpath:seed/alimentos.xlsx")).thenReturn(new ByteArrayResource(workbookBytes));

        TacoFoodSeedRunner runner = new TacoFoodSeedRunner(
            foodRepository,
            resourceLoader,
            true,
            "classpath:seed/alimentos.xlsx",
            "TACO",
            false
        );

        runner.run(new DefaultApplicationArguments(new String[0]));

        ArgumentCaptor<List<Food>> captor = ArgumentCaptor.forClass(List.class);
        verify(foodRepository).saveAll(captor.capture());
        List<Food> saved = captor.getValue();

        assertEquals(1, saved.size());
        assertEquals("Iogurte natural", saved.getFirst().getName());
        assertEquals(new BigDecimal("63.00"), saved.getFirst().getKcalPer100g());
    }

    @Test
    void runShouldTreatTrAsZero() throws Exception {
        byte[] workbookBytes = createWorkbookBytes(List.<String[]>of(
            new String[] { "3", "Cha mate", "0", "tr", "tr", "tr" }
        ));

        when(foodRepository.findBySourceIgnoreCase("TACO")).thenReturn(List.of());
        when(resourceLoader.getResource("classpath:seed/alimentos.xlsx")).thenReturn(new ByteArrayResource(workbookBytes));

        TacoFoodSeedRunner runner = new TacoFoodSeedRunner(
            foodRepository,
            resourceLoader,
            true,
            "classpath:seed/alimentos.xlsx",
            "TACO",
            false
        );

        runner.run(new DefaultApplicationArguments(new String[0]));

        ArgumentCaptor<List<Food>> captor = ArgumentCaptor.forClass(List.class);
        verify(foodRepository).saveAll(captor.capture());
        Food saved = captor.getValue().getFirst();

        assertEquals(new BigDecimal("0.00"), saved.getCarbsPer100g());
        assertEquals(new BigDecimal("0.00"), saved.getProteinPer100g());
        assertEquals(new BigDecimal("0.00"), saved.getFatPer100g());
    }

    private byte[] createWorkbookBytes(List<String[]> rows) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("TACO");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Numero");
            header.createCell(1).setCellValue("Descricao");
            header.createCell(2).setCellValue("Energia (kcal)");
            header.createCell(3).setCellValue("Proteina (g)");
            header.createCell(4).setCellValue("Lipideos (g)");
            header.createCell(5).setCellValue("Carboidrato (g)");

            int rowNum = 1;
            for (String[] data : rows) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < data.length; i++) {
                    row.createCell(i).setCellValue(data[i]);
                }
            }

            workbook.write(output);
            return output.toByteArray();
        }
    }
}
