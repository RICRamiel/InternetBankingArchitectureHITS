import { configureStore } from '@reduxjs/toolkit';
import { generatedApi } from '../api/generatedApi'; // этот файл будет создан генератором

export const store = configureStore({
  reducer: {
    [generatedApi.reducerPath]: generatedApi.reducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().concat(generatedApi.middleware),
});