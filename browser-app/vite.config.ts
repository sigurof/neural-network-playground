import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  // declare mat4 from external script
  // esbuild: {
  //   jsxInject: `import React from 'react'
  //       import { mat4 } from 'gl-matrix'`,
  // },
});
