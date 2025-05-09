export interface ConfigData {
  anon?: ConfigData;
  configData?: ConfigDataItem;
}

export interface ConfigDataItem {
  anon?: ConfigData;
  productConfiguration?: object;
  templates: object;
}
