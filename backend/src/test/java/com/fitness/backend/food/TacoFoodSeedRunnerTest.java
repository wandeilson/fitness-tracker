package com.fitness.backend.food;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
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
import org.springframework.core.io.Resource;
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
    void runShouldContinueWhenSkipIsDisabledEvenIfDataExists() throws Exception {
        byte[] workbookBytes = createWorkbookBytes(List.<String[]>of(
            new String[] { "1", "Arroz", "128", "2,5", "0,3", "28" }
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

        verify(foodRepository).saveAll(anyList());
    }

    @Test
    void runShouldSkipWhenResourceNotFound() throws Exception {
        Resource notFoundResource = mock(Resource.class);
        when(notFoundResource.exists()).thenReturn(false);

        when(foodRepository.countBySourceIgnoreCase("TACO")).thenReturn(0L);
        when(resourceLoader.getResource("classpath:seed/alimentos.xlsx")).thenReturn(notFoundResource);

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
    void runShouldThrowWhenWorkbookIsCorrupted() throws Exception {
        when(foodRepository.countBySourceIgnoreCase("TACO")).thenReturn(0L);
        when(resourceLoader.getResource("classpath:seed/alimentos.xlsx")).thenReturn(new ByteArrayResource(new byte[]{1, 2, 3}));

        TacoFoodSeedRunner runner = new TacoFoodSeedRunner(
            foodRepository,
            resourceLoader,
            true,
            "classpath:seed/alimentos.xlsx",
            "TACO",
            true
        );

        assertThrows(IllegalStateException.class, () -> runner.run(new DefaultApplicationArguments(new String[0])));
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
    void runShouldUpdateExistingFoodWhenNormalizedNameMatches() throws Exception {
        byte[] workbookBytes = createWorkbookBytes(List.<String[]>of(
            new String[] { "", "Iogurte Natural", "63", "3,9", "3,5", "4,7" }
        ));

        Food existing = new Food();
        existing.setSource("TACO");
        existing.setSourceCode("99");
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
        assertEquals(1, captor.getValue().size());
        assertEquals("Iogurte Natural", captor.getValue().getFirst().getName());
    }

    @Test
    void runShouldSkipBlankNameRows() throws Exception {
        byte[] workbookBytes = createWorkbookBytes(List.<String[]>of(
            new String[] { "1", "", "128", "2,5", "0,3", "28" },
            new String[] { "2", "  ", "76", "4,8", "0,5", "13,6" },
            new String[] { "3", "Feijao", "76", "4,8", "0,5", "13,6" }
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
        assertEquals(1, captor.getValue().size());
        assertEquals("Feijao", captor.getValue().getFirst().getName());
    }

    @Test
    void runShouldHandleBlankSourceCodeByTreatingAsNull() throws Exception {
        byte[] workbookBytes = createWorkbookBytes(List.<String[]>of(
            new String[] { "  ", "Macarrao", "353", "72,1", "11,1", "1,2" }
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
        assertEquals(1, captor.getValue().size());
    }

    @Test
    void runShouldHandleNullRowsInSheet() throws Exception {
        byte[] workbookBytes;
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("TACO");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Numero");
            header.createCell(1).setCellValue("Descricao");
            header.createCell(2).setCellValue("Energia (kcal)");
            header.createCell(3).setCellValue("Proteina (g)");
            header.createCell(4).setCellValue("Lipideos (g)");
            header.createCell(5).setCellValue("Carboidrato (g)");

            sheet.createRow(2);
            Row row = sheet.createRow(3);
            row.createCell(0).setCellValue("1");
            row.createCell(1).setCellValue("Leite");
            row.createCell(2).setCellValue("42");
            row.createCell(3).setCellValue("3,3");
            row.createCell(4).setCellValue("3,8");
            row.createCell(5).setCellValue("5,0");

            workbook.write(output);
            workbookBytes = output.toByteArray();
        }

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
        assertEquals(1, captor.getValue().size());
    }

    @Test
    void runShouldHandleNoHeaderRowFound() throws Exception {
        byte[] workbookBytes;
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("TACO");
            Row row = sheet.createRow(0);
            row.createCell(0).setCellValue("random data");

            workbook.write(output);
            workbookBytes = output.toByteArray();
        }

        when(resourceLoader.getResource("classpath:seed/alimentos.xlsx")).thenReturn(new ByteArrayResource(workbookBytes));

        TacoFoodSeedRunner runner = new TacoFoodSeedRunner(
            foodRepository,
            resourceLoader,
            true,
            "classpath:seed/alimentos.xlsx",
            "TACO",
            false
        );

        assertThrows(IllegalStateException.class, () -> runner.run(new DefaultApplicationArguments(new String[0])));
    }

    @Test
    void runShouldHandleEmptyHeaderRow() throws Exception {
        byte[] workbookBytes;
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("TACO");
            sheet.createRow(0);

            workbook.write(output);
            workbookBytes = output.toByteArray();
        }

        when(resourceLoader.getResource("classpath:seed/alimentos.xlsx")).thenReturn(new ByteArrayResource(workbookBytes));

        TacoFoodSeedRunner runner = new TacoFoodSeedRunner(
            foodRepository,
            resourceLoader,
            true,
            "classpath:seed/alimentos.xlsx",
            "TACO",
            false
        );

        assertThrows(IllegalStateException.class, () -> runner.run(new DefaultApplicationArguments(new String[0])));
    }

    @Test
    void runShouldHandleFallbackColumnIndexesWhenHeadersDontMatch() throws Exception {
        byte[] workbookBytes;
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("TACO");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Code");
            header.createCell(1).setCellValue("Item");
            header.createCell(2).setCellValue("Notes");
            header.createCell(3).setCellValue("Energy");
            header.createCell(4).setCellValue("Fat");
            header.createCell(5).setCellValue("Protein");
            header.createCell(6).setCellValue("Fiber");
            header.createCell(7).setCellValue("Sugar");
            header.createCell(8).setCellValue("Carbs");
            header.createCell(9).setCellValue("Other");

            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("1");
            row.createCell(1).setCellValue("Banana");
            row.createCell(3).setCellValue("89");
            row.createCell(5).setCellValue("1,1");
            row.createCell(6).setCellValue("0,2");
            row.createCell(8).setCellValue("23");

            workbook.write(output);
            workbookBytes = output.toByteArray();
        }

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
        assertEquals(1, captor.getValue().size());
        assertEquals("Banana", captor.getValue().getFirst().getName());
    }

    @Test
    void runShouldHandleFallbackIndexesForShortRow() throws Exception {
        byte[] workbookBytes;
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("TACO");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("CustomCode");
            header.createCell(1).setCellValue("CustomName");
            header.createCell(2).setCellValue("Extra");
            header.createCell(3).setCellValue("Energy");
            header.createCell(4).setCellValue("Fat");
            header.createCell(5).setCellValue("Protein");
            header.createCell(6).setCellValue("Fiber");
            header.createCell(7).setCellValue("Sugar");
            header.createCell(8).setCellValue("Carbs");

            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("5");
            row.createCell(1).setCellValue("Batata");

            workbook.write(output);
            workbookBytes = output.toByteArray();
        }

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

        verify(foodRepository).saveAll(anyList());
    }

    @Test
    void runShouldHandleBlankRowAfterName() throws Exception {
        byte[] workbookBytes;
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("TACO");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Numero");
            header.createCell(1).setCellValue("Descricao");
            header.createCell(2).setCellValue("Energia (kcal)");
            header.createCell(3).setCellValue("Proteina (g)");
            header.createCell(4).setCellValue("Lipideos (g)");
            header.createCell(5).setCellValue("Carboidrato (g)");

            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("1");
            row1.createCell(1).setCellValue("Banana");
            row1.createCell(2).setCellValue("89");
            row1.createCell(3).setCellValue("1,1");
            row1.createCell(4).setCellValue("0,3");
            row1.createCell(5).setCellValue("23");
            Row row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("2");
            row2.createCell(1).setCellValue("  ");
            row2.createCell(2).setCellValue("0");
            row2.createCell(3).setCellValue("0");
            row2.createCell(4).setCellValue("0");
            row2.createCell(5).setCellValue("0");

            workbook.write(output);
            workbookBytes = output.toByteArray();
        }

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
        assertEquals(1, captor.getValue().size());
    }

    @Test
    void runShouldHandleBlankSourceCodeAsNullAndNotMatch() throws Exception {
        byte[] workbookBytes = createWorkbookBytes(List.<String[]>of(
            new String[] { "", "Cafe", "340", "13,2", "12,3", "0,8" }
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
        assertEquals("Cafe", captor.getValue().getFirst().getName());
    }

    @Test
    void runShouldHandleLongNameByTruncating() throws Exception {
        String longName = "A".repeat(300);
        byte[] workbookBytes = createWorkbookBytes(List.<String[]>of(
            new String[] { "1", longName, "128", "2,5", "0,3", "28" }
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
        assertEquals(255, captor.getValue().getFirst().getName().length());
    }

    @Test
    void runShouldHandleLongSourceCodeByTruncating() throws Exception {
        String longCode = "C".repeat(80);
        byte[] workbookBytes = createWorkbookBytes(List.<String[]>of(
            new String[] { longCode, "Alface", "15", "1,2", "0,2", "2,9" }
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
        assertEquals(50, captor.getValue().getFirst().getSourceCode().length());
    }

    @Test
    void runShouldHandleNullNameInRow() throws Exception {
        byte[] workbookBytes;
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("TACO");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Numero");
            header.createCell(1).setCellValue("Descricao");
            header.createCell(2).setCellValue("Energia (kcal)");
            header.createCell(3).setCellValue("Proteina (g)");
            header.createCell(4).setCellValue("Lipideos (g)");
            header.createCell(5).setCellValue("Carboidrato (g)");

            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("1");

            workbook.write(output);
            workbookBytes = output.toByteArray();
        }

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

        verify(foodRepository, never()).saveAll(anyList());
    }

    @Test
    void runShouldHandleMixedBlankAndValidSourceCodes() throws Exception {
        byte[] workbookBytes;
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("TACO");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Numero");
            header.createCell(1).setCellValue("Descricao");
            header.createCell(2).setCellValue("Energia (kcal)");
            header.createCell(3).setCellValue("Proteina (g)");
            header.createCell(4).setCellValue("Lipideos (g)");
            header.createCell(5).setCellValue("Carboidrato (g)");

            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("  ");
            row.createCell(1).setCellValue("Leite");
            row.createCell(2).setCellValue("42");
            row.createCell(3).setCellValue("3,3");
            row.createCell(4).setCellValue("3,8");
            row.createCell(5).setCellValue("5");

            workbook.write(output);
            workbookBytes = output.toByteArray();
        }

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

        verify(foodRepository).saveAll(anyList());
    }

    @Test
    void runShouldHandleNullCellsInRow() throws Exception {
        byte[] workbookBytes;
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("TACO");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Numero");
            header.createCell(1).setCellValue("Descricao");
            header.createCell(2).setCellValue("Energia (kcal)");
            header.createCell(3).setCellValue("Proteina (g)");
            header.createCell(4).setCellValue("Lipideos (g)");
            header.createCell(5).setCellValue("Carboidrato (g)");

            Row row = sheet.createRow(1);

            workbook.write(output);
            workbookBytes = output.toByteArray();
        }

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

        verify(foodRepository, never()).saveAll(anyList());
    }

    @Test
    void runShouldHandleDashValueAsZero() throws Exception {
        byte[] workbookBytes = createWorkbookBytes(List.<String[]>of(
            new String[] { "1", "Teste", "100", "-", "5", "-" }
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
        assertEquals(new BigDecimal("0.00"), captor.getValue().getFirst().getProteinPer100g());
    }

    @Test
    void runShouldHandleDotOnlyValueAsZero() throws Exception {
        byte[] workbookBytes = createWorkbookBytes(List.<String[]>of(
            new String[] { "1", "Teste", "100", ".", "5", "10" }
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
        assertEquals(new BigDecimal("0.00"), captor.getValue().getFirst().getProteinPer100g());
    }

    @Test
    void runShouldHandleEmptyCellAsBlank() throws Exception {
        byte[] workbookBytes;
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("TACO");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Numero");
            header.createCell(1).setCellValue("Descricao");
            header.createCell(2).setCellValue("Energia (kcal)");
            header.createCell(3).setCellValue("Proteina (g)");
            header.createCell(4).setCellValue("Lipideos (g)");
            header.createCell(5).setCellValue("Carboidrato (g)");

            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("1");
            row.createCell(1).setCellValue("Teste");
            row.createCell(2).setCellValue("100");

            workbook.write(output);
            workbookBytes = output.toByteArray();
        }

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

        verify(foodRepository).saveAll(anyList());
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
