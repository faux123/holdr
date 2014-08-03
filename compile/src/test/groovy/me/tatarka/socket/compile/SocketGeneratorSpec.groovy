package me.tatarka.socket.compile

import spock.lang.Shared
import spock.lang.Specification

import static me.tatarka.socket.compile.SpecHelpers.code

class SocketGeneratorSpec extends Specification {
    @Shared
    SocketGenerator generator = new SocketGenerator("me.tatarka.test")

    def "an empty list of views generates an empty Socket"() {
        expect:
        code(generator, "test", [] as Set) == """
package me.tatarka.test.sockets;
$IMPORTS
public class SocketTest
    extends Socket
{

    public final static int LAYOUT = R.layout.test;

    /**
     * Constructs a new {@link me.tatarka.socket.Socket} for {@link me.tatarka.test.R.layout#test}.
     * 
     * @param view
     *     The root view to search for the socket's views.
     */
    public SocketTest(View view) {
        super(view);
    }

}
"""
    }

    def "a single view generates a Socket that instantiates that view"() {
        expect:
        code(generator, "test", [View.of("android.widget.TextView", "my_text_view").build()] as Set) == """
package me.tatarka.test.sockets;
$IMPORTS
public class SocketTest
    extends Socket
{

    public final static int LAYOUT = R.layout.test;
    /**
     * View for {@link me.tatarka.test.R.id#my_text_view}.
     * 
     */
    public android.widget.TextView myTextView;

    /**
     * Constructs a new {@link me.tatarka.socket.Socket} for {@link me.tatarka.test.R.layout#test}.
     * 
     * @param view
     *     The root view to search for the socket's views.
     */
    public SocketTest(View view) {
        super(view);
        myTextView = ((android.widget.TextView) view.findViewById(R.id.my_text_view));
    }

}
"""
    }

    def "a view with a custom field name uses that name"() {
        expect:
        code(generator, "test", [View.of("android.widget.TextView", "my_text_view").fieldName("myCustomField").build()] as Set) == """
package me.tatarka.test.sockets;
$IMPORTS
public class SocketTest
    extends Socket
{

    public final static int LAYOUT = R.layout.test;
    /**
     * View for {@link me.tatarka.test.R.id#my_text_view}.
     * 
     */
    public android.widget.TextView myCustomField;

    /**
     * Constructs a new {@link me.tatarka.socket.Socket} for {@link me.tatarka.test.R.layout#test}.
     * 
     * @param view
     *     The root view to search for the socket's views.
     */
    public SocketTest(View view) {
        super(view);
        myCustomField = ((android.widget.TextView) view.findViewById(R.id.my_text_view));
    }

}
"""
    }

    def "a view with an android id uses android.R.id instead of R.id"() {
        expect:
        code(generator, "test", [View.of("android.widget.TextView", "text1").androidId().build()] as Set) == """
package me.tatarka.test.sockets;

import android.view.View;
import me.tatarka.socket.Socket;

public class SocketTest
    extends Socket
{

    public final static int LAYOUT = me.tatarka.test.R.layout.test;
    /**
     * View for {@link android.R.id#text1}.
     * 
     */
    public android.widget.TextView text1;

    /**
     * Constructs a new {@link me.tatarka.socket.Socket} for {@link me.tatarka.test.R.layout#test}.
     * 
     * @param view
     *     The root view to search for the socket's views.
     */
    public SocketTest(View view) {
        super(view);
        text1 = ((android.widget.TextView) view.findViewById(android.R.id.text1));
    }

}
"""
    }

    def "an include with an id generates a reference to a socket with it's layout"() {
        expect:
        code(generator, "test", [Include.of("my_layout", "my_include").build()] as Set) == """
package me.tatarka.test.sockets;
$IMPORTS
public class SocketTest
    extends Socket
{

    public final static int LAYOUT = R.layout.test;
    /**
     * Socket for {@link me.tatarka.test.R.layout#my_layout}.
     * 
     */
    public SocketMyLayout myInclude;

    /**
     * Constructs a new {@link me.tatarka.socket.Socket} for {@link me.tatarka.test.R.layout#test}.
     * 
     * @param view
     *     The root view to search for the socket's views.
     */
    public SocketTest(View view) {
        super(view);
        myInclude = new SocketMyLayout(view);
    }

}
"""
    }

    private static final String IMPORTS = """
import android.view.View;
import me.tatarka.socket.Socket;
import me.tatarka.test.R;
"""
}
