package me.tatarka.holdr.compile

import spock.lang.Shared
import spock.lang.Specification

import static SpecHelpers.xml

class HoldrViewParserSpec extends Specification {
    @Shared
    def parser = new HoldrViewParser(true)

    def "a single non-id view parses as an empty list"() {
        expect:
        parser.parse(xml { it.'TextView'() }) == []
    }

    def "a single view with an id parses as a single item"() {
        expect:
        parser.parse(xml {
            it.'TextView'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'android:id': '@+id/my_text_view'
            )
        }) == [View.of('android.widget.TextView', 'my_text_view').build()]
    }

    def "a non-id view with 2 children parses as two items"() {
        expect:
        parser.parse(xml {
            it.'LinearLayout'('xmlns:android': 'http://schemas.android.com/apk/res/android') {
                'TextView'('android:id': '@+id/my_text_view')
                'ImageView'('android:id': '@+id/my_image_view')
            }
        }) == [
                View.of('android.widget.TextView', 'my_text_view').build(),
                View.of('android.widget.ImageView', 'my_image_view').build()
        ]
    }

    def "an id view with non-id children parses as a single item"() {
        expect:
        parser.parse(xml {
            it.'LinearLayout'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'android:id': '@+id/my_linear_layout'
            ) {
                'TextView'()
                'ImageView'()
            }
        }) == [
                View.of('android.widget.LinearLayout', 'my_linear_layout').build()
        ]
    }

    def "an id view with id children parses as a list of items"() {
        expect:
        parser.parse(xml {
            it.'LinearLayout'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'android:id': '@+id/my_linear_layout'
            ) {
                'TextView'('android:id': '@+id/my_text_view')
                'ImageView'('android:id': '@+id/my_image_view')
            }
        }) == [
                View.of('android.widget.LinearLayout', 'my_linear_layout').build(),
                View.of('android.widget.TextView', 'my_text_view').build(),
                View.of('android.widget.ImageView', 'my_image_view').build()
        ]
    }

    def "a view with an id but has a 'holdr_ignore=view' is not included"() {
        expect:
        parser.parse(xml {
            it.'TextView'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'xmlns:app': 'http://schemas.android.com/apk/res-auto',
                    'android:id': '@+id/my_text_view',
                    'app:holdr_ignore': 'view'
            )
        }) == []
    }

    def "a view with an id but has a 'holdr_ignore=all' does not include itself or it's children"() {
        expect:
        parser.parse(xml {
            it.'LinearLayout'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'xmlns:app': 'http://schemas.android.com/apk/res-auto',
                    'android:id': '@+id/my_linear_layout',
                    'app:holdr_ignore': 'all'
            ) {
                'TextView'('android:id': '@+id/my_text_view')
                'ImageView'('android:id': '@+id/my_image_view')
            }
        }) == []
    }
    
    def "a view with 'holdr_ignore=all' can have a child with 'holdr_include=view' which will be included"() {
        expect:
        parser.parse(xml {
            it.'LinearLayout'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'xmlns:app': 'http://schemas.android.com/apk/res-auto',
                    'android:id': '@+id/my_linear_layout',
                    'app:holdr_ignore': 'all'
            ) {
                'TextView'('android:id': '@+id/my_text_view', 'app:holdr_include': 'view')
                'ImageView'('android:id': '@+id/my_image_view')
            }
        }) == [View.of('android.widget.TextView', 'my_text_view').build()]
    }
    
    def "a view with 'holdr_ignore=all' can have a child with 'holdr_include=all' which will include all it's chilren"() {
        expect:
        parser.parse(xml {
            it.'LinearLayout'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'xmlns:app': 'http://schemas.android.com/apk/res-auto',
                    'android:id': '@+id/my_linear_layout',
                    'app:holdr_ignore': 'all'
            ) {
                'ImageView'('android:id': '@+id/my_image_view')
                'LinearLayout'('android:id': '@+id/my_child_linear_layout', 'app:holdr_include': 'all') {
                    'TextView'('android:id': '@+id/my_text_view')
                }
            }
        }) == [
                View.of('android.widget.LinearLayout', 'my_child_linear_layout').build(),
                View.of('android.widget.TextView', 'my_text_view').build(),
        ]
    }

    def "a view with a 'holdr_field_name' attribute has a custom field name"() {
        expect:
        parser.parse(xml {
            it.'TextView'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'xmlns:app': 'http://schemas.android.com/apk/res-auto',
                    'android:id': '@+id/my_text_view',
                    'app:holdr_field_name': 'my_field_name'
            )
        }) == [View.of('android.widget.TextView', 'my_text_view').fieldName('my_field_name').build()]
    }

    def "a view with and android id parses as an item that knows this"() {
        expect:
        parser.parse(xml {
            it.'TextView'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'android:id': '@android:id/text1',
            )
        }) == [View.of('android.widget.TextView', 'text1').androidId().build()]
    }

    def "an include with an id parses as an include item"() {
        expect:
        parser.parse(xml {
            it.'include'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'android:id': '@+id/my_include',
                    'layout': '@layout/my_layout'
            )
        }) == [Include.of('my_layout', 'my_include').build()]
    }
    
    def "an unqualified view in the 'view' namespace parses with the correct prefix"() {
        expect:
        parser.parse(xml {
            it."$view"(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'android:id': '@+id/my_id',
            )
        }).first().type == "android.view.$view"

        where:
        view << ['View', 'SurfaceView', 'TextureView', 'ViewStub']
    }
    
    def "an unqualified view in the 'webkit' namespace parses with the correct prefix"() {
        expect:
        parser.parse(xml {
            it."$view"(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'android:id': '@+id/my_id',
            )
        }).first().type == "android.webkit.$view"
        
        where:
        view << ['WebView']
    }
    
    def "an unqualified view in the 'widget' namespace parses with the correct prefix"() {
        expect:
        parser.parse(xml {
            it."$view"(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'android:id': '@+id/my_id',
            )
        }).first().type == "android.widget.$view"

        where:
        view << ['TextView', 'Button', 'ImageButton', 'EditText', 'ImageView', 'FrameLayout', 'LinearLayout', 'GridLayout']
    }
}