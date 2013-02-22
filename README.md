LazySpring
====

Intellij IDEA Plugin for context-config generation for class/test dependencies.
Useful for quick dependencies resolution in JUnit-tests with Spring-beans.

    Detects class dependencies that must be resolved with spring-beans.<br>
    There are fields annotated with @Autowired or class's setters(set-methods)<br>
    Searchs bean definitions through all project *.xml files, that can be assigned to them<br>
    For all beans it recursively detects their dependencies and doing another search<br>
    Finally, we have all beans that can be need for our initial class<br>
    They can be saved to some xml-file and used as context-configuration for our class<br>
    <br>
    Main use-case: Quick context-config generation for JUnit-tests.<br>
    In big project it can be difficult(or boring) to resolve all class dependencies to run some local-tests<br>
    <br>
    If more than one beans found by name and type, it will be resolved with some priority-rules that you can tune in plugin-propertis<br>
    <br>
    Supported features:
    <ul>
        <li>Searching beans in production, test or libraries scope.</li>
        <li>Searching beans with a check for assign by field-type (not only by name)</li>
        <li>Bean-aliases supported. We can find bean-alias by name and another bean that can be referenced by alias</li>
        <li>Spring-util beans supported: map, set, list</li>
        <li>Spring-util beans supported: map, set, list</li>
        <li>Spring-util beans supported: map, set, list</li>
        <li>Some beans can be excludes from search by their name through the plugin-properties</li>
        <li>Some beans can be described for use strongly-their</li>
        <li>Bean's file-path can have priority, that used to resolve conflicts if more then one bean were found</li>
        <li>Can be added custom headers in result-file, e.g. we can exclude some useful beans and add <import> with it file in header</li>
    </ul>
    <br>Default shortcuts:
    <ul>
        <li>Ctrl+Shift+Alt+L - Generate context-configuration for current class(in editor)</li>
        <li>Tools Menu -&gt; "Generate context config" - Same as above</li>
        <li>Settings Menu (Ctrl+Alt+S) -&gt; LazySpring - Plugin properties</li>
    </ul>
    <br>Please note: This plugin is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    
