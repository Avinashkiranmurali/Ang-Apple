import { Product } from './product';

export interface Carousel {
    name: string;
    displayOrder: number;
    config: CarouselConfig;
    products: Product[];
}

export interface CarouselConfig {
    showNavigationArrows: boolean;
    showNavigationIndicators: boolean;
    backgroundColor: string;
    showCTA: boolean;
    wrapperClass: string;
    tileBackgroundColor: string;
    tileCTABackgroundColor: string;
    tileCTATextColor: string;
    titleText: string;
    ctaLabelText: string;
    ctaLabelStyle: string;
    ctaTargetType: string;
    styleType?: string;
    showOnlyOnEmptyCart?: boolean;
    hideStrikethroughPrice?: boolean;
}
