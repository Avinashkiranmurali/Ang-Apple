package com.b2s.apple.mapper

import com.b2s.apple.services.CategoryConfigurationService
import com.b2s.rewards.apple.model.Option
import spock.lang.Specification
import spock.lang.Subject

import static com.b2s.rewards.common.util.CommonConstants.QUALITY_SWATCH_IMAGE_URL

class OptionMapperSpec extends Specification {

    CategoryConfigurationService categoryConfigurationService = Mock(CategoryConfigurationService)

    @Subject
    OptionMapper optionMapper = new OptionMapper(categoryConfigurationService: categoryConfigurationService)

    def 'test quality swatchImageUrl'() {
        when:
        Option result = optionMapper.createOption(optionName, 'Nikeblack', Optional.of('nikeblack'), 'Color', 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/MXN12_SW_COLOR?wid=32&hei=32&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1583175347027')

        then:
        result.getSwatchImageUrl() == finalSwatchImageUrl

        where:
        optionName || finalSwatchImageUrl
        'color'    || 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/MXN12_SW_COLOR?wid=32&hei=32&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1583175347027' + QUALITY_SWATCH_IMAGE_URL
        'storage'  || 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/MXN12_SW_COLOR?wid=32&hei=32&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1583175347027'
    }

}
