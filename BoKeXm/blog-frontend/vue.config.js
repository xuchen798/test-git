const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    host: 'localhost',
    port: 8082,
    open: true,
    allowedHosts: 'all',
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      '/upload': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        pathRewrite: { '^/upload': '/api/upload' }
      }
    }
  }
})
