export enum MealType {
  BREAKFAST = 'BREAKFAST',
  MORNING_SNACK = 'MORNING_SNACK',
  LUNCH = 'LUNCH',
  AFTERNOON_SNACK = 'AFTERNOON_SNACK',
  DINNER = 'DINNER',
  SUPPER = 'SUPPER',
}

export interface MealItemResponse {
  id: number;
  foodId: number;
  foodName: string;
  grams: number;
  kcalConsumed: number;
  carbsConsumed: number;
  proteinConsumed: number;
  fatConsumed: number;
}

export interface MealResponse {
  id: number;
  mealDate: string;
  mealType: MealType;
  consumedAt: string | null;
  notes: string | null;
  kcalTotal: number;
  carbsTotal: number;
  proteinTotal: number;
  fatTotal: number;
  items: MealItemResponse[];
}

export interface MealCreateRequest {
  mealDate: string;
  mealType: MealType;
  consumedAt?: string;
  notes?: string;
}

export interface MealItemRequest {
  foodId: number;
  grams: number;
}

export interface GoalSummary {
  calories: number;
  carbsG: number;
  proteinG: number;
  fatG: number;
}

export interface DailySummaryResponse {
  date: string;
  kcalTotal: number;
  carbsTotal: number;
  proteinTotal: number;
  fatTotal: number;
  goal: GoalSummary | null;
}

export const MEAL_TYPE_LABELS: Record<MealType, string> = {
  [MealType.BREAKFAST]: 'Café da manhã',
  [MealType.MORNING_SNACK]: 'Lanche da manhã',
  [MealType.LUNCH]: 'Almoço',
  [MealType.AFTERNOON_SNACK]: 'Lanche da tarde',
  [MealType.DINNER]: 'Jantar',
  [MealType.SUPPER]: 'Ceia',
};

export const MEAL_TYPE_ORDER: MealType[] = [
  MealType.BREAKFAST,
  MealType.MORNING_SNACK,
  MealType.LUNCH,
  MealType.AFTERNOON_SNACK,
  MealType.DINNER,
  MealType.SUPPER,
];
