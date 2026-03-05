const { getDefaultConfig } = require("expo/metro-config");
const path = require("path");

const projectRoot = __dirname;
const backendRoot = path.resolve(projectRoot, "../backend");

const config = getDefaultConfig(projectRoot);

// Allow Metro to resolve modules from the backend directory
config.watchFolders = [backendRoot];

// Make sure the backend's node_modules are resolved correctly
config.resolver.nodeModulesPaths = [
    path.resolve(projectRoot, "node_modules"),
    path.resolve(backendRoot, "node_modules"),
];

module.exports = config;
