import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// `npm run build` writes straight into Spring Boot's static folder, so the backend serves the UI.
// `npm run dev` runs a hot-reload server on :5173 that forwards /api calls to the backend on :8080.
export default defineConfig({
  plugins: [react()],
  build: {
    outDir: '../src/main/resources/static',
    emptyOutDir: true,
  },
  server: {
    proxy: { '/api': 'http://localhost:8080' },
  },
});
