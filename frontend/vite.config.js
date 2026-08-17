// This file configures Vite, the tool that runs our React project.
// Vite does two jobs: it starts a local web server for development
// (on http://localhost:5173), and it converts our React files into
// normal HTML/CSS/JS that the browser understands.
// You will never need to edit this file.
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
})
