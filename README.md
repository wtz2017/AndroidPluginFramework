# AndroidPluginFramework
这是一个简单插件框架示例工程； 
各个模块作用： 
plugin_sdk 为其它模块提供公共接口和属性，并负责更新和加载核心框架 plugin_framework ； 
plugin_framework 是插件框架核心，负责更新和加载插件（比如 plugin1 ）； 
plugin1 是一个插件应用示例； 
app 是整个插件框架所依赖的宿主应用程序，插件的代理类就是在其中注册； 
provided_jars 是专门给插件模块（比如plugin1）使用，目的是提供一些插件编译时引用但又不需要打包的类，目前直接使用android studio中的provided选项时，编译时会报错： Error:Project :plugin1: Provided dependencies can only be jars. com.android.support:support-core-ui:26.0.0-alpha1@aar is an Android Library.
