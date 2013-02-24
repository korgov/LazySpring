LazySpring
====

Intellij IDEA Plugin for context-config generation for class/test dependencies.
Useful for quick dependencies resolution in JUnit-tests with Spring-beans.

    Detects class dependencies that must be resolved with spring-beans.
    There are fields annotated with @Autowired or class's setters(set-methods)
    Searchs bean definitions through all project *.xml files, that can be assigned to them
    For all beans it recursively detects their dependencies and doing another search
    Finally, we have all beans that can be need for our initial class
    They can be saved to some xml-file and used as context-configuration for our class
    
    Main use-case: Quick context-config generation for JUnit-tests.
    In big project it can be difficult(or boring) to resolve all class dependencies to run some local-tests
    
    If more than one beans found by name and type, it will be resolved with some priority-rules 
    that you can tune in plugin-propertis
    
    Supported features:
    
        * Searching beans in production, test or libraries scope.
        * Searching beans with a check for assign by field-type (not only by name)
        * Bean-aliases supported. We can find bean-alias by name and another bean 
          that can be referenced by alias
        * Spring-util beans supported: map, set, list
        * Spring-util beans supported: map, set, list
        * Spring-util beans supported: map, set, list
        * Some beans can be excludes from search by their name through the plugin-properties
        * Some beans can be described for use strongly-their
        * Bean's file-path can have priority, that used to resolve conflicts if more then one bean were found
        * Can be added custom headers in result-file, e.g. we can exclude some useful beans 
          and add <import> with it file in header
    
    Default shortcuts:
    
        * Ctrl+Shift+Alt+L - Generate context-configuration for current class(in editor)
        * Tools Menu -&gt; "Generate context config" - Same as above
        * Settings Menu (Ctrl+Alt+S) -&gt; LazySpring - Plugin properties
    
    Please note: This plugin is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied.


