module.exports = {
  root: true,
  env: {
    es6: true,
    node: true,
  },
  extends: [
    "eslint:recommended",
    "plugin:import/errors",
    "plugin:import/warnings",
    "plugin:import/typescript",
    "google",
    "plugin:@typescript-eslint/recommended",
  ],
  parser: "@typescript-eslint/parser",
  parserOptions: {
    project: ["tsconfig.json", "tsconfig.dev.json"],
    sourceType: "module",
    tsconfigRootDir: __dirname, // 이 줄을 추가하면 기준 디렉토리 문제가 해결됩니다.
  },
  ignorePatterns: [
    "/lib/**/*", // 빌드된 js 파일 무시
    "**/*.js",    // 루트 폴더의 js 파일 무시
  ],
  plugins: [
    "@typescript-eslint",
    "import",
  ],
  rules: {
      "quotes": ["error", "double"],
      "import/no-unresolved": 0,
      "indent": ["error", 2],
      "max-len": "off", // 최대 길이 제한 끄기
      "@typescript-eslint/no-unused-vars": "warn", // 사용하지 않는 변수 오류를 경고로 변경
      "eol-last": "off", // 파일 끝에 개행 문자 강제 끄기
       "padded-blocks": "off",
    "@typescript-eslint/no-explicit-any": "off",

    },
};