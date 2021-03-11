'use strict'
const merge = require('webpack-merge')
const prodEnv = require('./prod.env')

module.exports = merge(prodEnv, {
  NODE_ENV: '"development"',
  SERVICE_BASE_URL: '"http://127.0.0.1:8080"',
  PARTNER_ID: '"5e893b34dd9227b36629327d2afe380"',
  PARTNER_KEY: '"2cb7737ab13d9abca880f8cf4f6a840c2611ae96"'
})
