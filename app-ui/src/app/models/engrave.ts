export interface Engrave {
  line1: string;
  line2: string;
  font: string;
  fontCode: string;
  maxCharsPerLine: string;
  widthDimension: string;
  noOfLines: number;
  engraveBgImageLocation?: string;
  isSkuBasedEngraving: boolean;
  templateClass?: string;
  preview: boolean;
  isUpperCaseEnabled: boolean;
  isDefaultPreviewEnabled: boolean;
  previewUrl?: string;
  engraveFontConfigurations?: EngraveFontConfiguration[];
}

export interface EngraveFontConfiguration {
  id: number;
  engrave_config_id: number;
  char_length_form: number;
  char_length_to: number;
  font_code: string;
}
