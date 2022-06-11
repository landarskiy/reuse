# ReUse
[![latestVersion](https://jitpack.io/v/landarskiy/reuse.svg)](https://jitpack.io/#landarskiy/reuse)
![MinSdk](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)

ReUse is a helper library for RecyclerView that makes displaying different type of data through RecyclerView easily. Common idea - avoid directly using adapters and one place for creating and binding ViewHolders. You should follow next steps for makes your life less painful and easily.

### ReuseViewHolder

`ReuseViewHolder` is a regular `ViewHolder` with some specific fields, methods and parameterized with your own class which used in generated code and for binding some information into your `ViewHolder`. You should create it with the same logic as usual with some differences:
* extend `ReuseViewHolder` instead regular `RecyclerView.ViewHolder` 
* parametrize by your entry class which you will use for provide data to your `ReuseViewHolder`
* implement `bind()` (with or without payload) method which receive typed entry specified in generic

```kotlin
class TextViewHolder(view: View) : ReuseViewHolder<TextEntry>(view) {

    private val textView: TextView = view as TextView

    override fun bind(entry: TextEntry) {
        textView.text = entry.content.text
    }
}
```

### ViewHolderFactory

`ViewHolderFactory` is a factory which response to create specific `ReuseViewHolder` for your data object with specified view type. This approach allows to you create many UI representations for same specific data type. You can use default implementation which use layout resource as `typeId` and inflate View automaticly:

```kotlin
@ReuseFactory
class TextViewHolderFactory : LayoutViewHolderFactory<TextEntry>(R.layout.item_text) {

    override fun createViewHolder(view: View): ReuseViewHolder<TextEntry> {
        return TextViewHolder(view)
    }
}
```

Or you can use implementation without support layout resources:

```kotlin
@ReuseFactory
class TextViewHolderFactory : ViewHolderFactory<TextEntry> {

    //in this case recommended use resource id for typeId field for avoid collisions 
    override val typeId: Int = R.id.your_type_id
    
    override fun createViewHolder(view: View): ReuseViewHolder<TextEntry> {
        return TextViewHolder(view)
    }

    override fun createView(context: Context, parent: ViewGroup?): View {
        return TextView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }
}
```

Each `ViewHolderFactory` should be annotated via `@ReuseFactory` annotation. This annotation inform compiler that it should create data builder for this view type with specified data class.

You can specify scope for group some `ViewHolderFactory` in specific data builder use `scopes` annotation parameter:

```kotlin
@ReuseFactory(scopes = ["text", "preview"])
class TextViewHolderFactory : ViewHolderFactory<TextEntry> {
    //some implementation
}
```

By default for each `ReuseFactory` will be generated 2 methods for build data list named `with[FactoryName]` with single or list items as argument. E.g. for `TextViewHolderFactory` will be generated `withTextViewHolderFactory(data: TextEntry): DataBuilder` and `withTextViewHolderFactory(data: Lis<TextEntry>): DataBuilder`. You can specify another name by `name` annotation parameter:

```kotlin
@ReuseFactory(name = "text", scopes = ["text", "preview"])
class TextViewHolderFactory : ViewHolderFactory<TextEntry> {
    //some implementation
}
```

In this case will be generated `withText(data: TextEntry): DataBuilder` and `withText(data: Lis<TextEntry>): DataBuilder` methods.

### ReuseModule

For inform compiler about package where will be places generated data builders you should create empty interface with `ReuseModule` annotation:

```kotlin
@ReuseModule
interface ReuseModule
```
After build project compiler will generate scope classes. By default if you not specify any scope in your `ReuseFactory` it will put into `ReuseDefaultContentScope()`. If you have custom scopes they will be grouped in `Reuse[scopeName]ContentScope()` class. 

Each content scope have `DataBuilder` class for type safe generate data for using in adapters.

### DiffEntry

In cases when you want to use default implementations of adapters which support DiffUtils you can implement your data classes which you specify as a generic argument in `ReuseViewHolder` and `ViewHolderFactory` as `DiffEntry` interface. 

```kotlin
data class TextEntry(val content: Content.Text) : DiffEntry {

    override fun isSameEntry(other: DiffEntry): Boolean {
        if (other !is TextEntry) {
            return false
        }
        return content == other.content
    }

    override fun isSameContent(other: DiffEntry): Boolean {
        return true
    }
}
```

`DiffEntry` interface have following methods:
- `isSameEntry(other: DiffEntry): Boolean` - to decide whether two object represent the same `DiffEntry`.
- `isSameContent(other: DiffEntry): Boolean` - to check whether two entries have the same content.
- `getDiffPayload(other: DiffEntry): Any?` - to get a payload about the change.

*It's recommended separate your data and entry classes. `DiffEntry` usefull for transfer data from your source to ViewHolder and it's also useful using it for provide some listeners and another things which not related 
your data classes but shoul be pass into ViewHolder for make some work (e.g. handle click on some UI controls).*

### TypedDiffEntry

In cases when you sure that specific `DiffEntry` will receive the same object type in `isSameEntry`, `isSameContent` methods (e.g. you use `DataBuilder` classes for data updates) you can use `TypedDiffEntry` instead.

`TypedDiffEntry` extends `DiffEntry` and provide typed versions of it. New methods have the same names with `Typed` suffix in the ending.

```kotlin
data class TextEntry(val content: Content.Text) : TypedDiffEntry<TextEntry>() {

    override fun isSameEntryTyped(other: TextEntry): Boolean {
        return content == other.content
    }

    override fun isSameContentTyped(other: TextEntry): Boolean {
        return true
    }
}
```

### DefaultContentScope

After you create all needed `ReuseViewHolder` and `ViewHolderFactory` classes you can update adapter's data use a few lines of code:

```kotlin
class MainActivity : AppCompatActivity() {
    
    private val defaultScope: ReuseDefaultContentScope = ReuseDefaultContentScope()
    private val listAdapter: AsyncDiffAdapter = AsyncDiffAdapter(defaultScope.types)
        
    fun updateData() {
        val dataBuilder = defaultScope.newDataBuilder()
        dataBuilder.withTextViewHolderFactory(TextEntry(Content.Text("Some text", Content.Text.Style.H3))
        dataBuilder.withTexGrouptViewHolderFactory(TextGroupEntry(Content.GroupHeader(true)) {
            viewModel.onGroupClicked()
        })
        //add data use generated methods
        listAdapter.submitList(dataBuilder.build())
    }
}
```

### Adapters

For use default adapters you need to add follow dependency to project:


```groovy
implementation "com.github.landarskiy.reuse:reuse-adapter:$reuse_version"
```

After that you will have access to few default adapters:

- `Adapter<T>` base adapter which can be specified any type of class.
- `DefaultAdapter` is `Adapter` specified by `Any` class.
- `DiffAdapter` adapter which support `DiffUtil` and paramertized by `DiffEntry` impleemntations.
- `AsyncDiffAdapter` is `ListAdapter` which parametrized by `DiffEntry` impleemntations.

## KSP options

You can specify some options during generation use ksp block in your build.gradle file

```groovy
ksp {
    // Key: reuseDefaultScopeMode
    // Value: always - always generate default scope and put to it all ViewHolder factories
    // Value: emptyScopes - default value, put into default scope only ViewHolder factories which don't have specific scopes
    // Value: never - never generate default scope
    arg("reuseDefaultScopeMode", "emptyScopes")
    // Key: reuseCheckFactoryInstance
    // Value: true - default value, check each @ReuseFactory supertype. Can little slow down generation.
    // Value: false - skip checking each @ReuseFactory supertype. Can little speed up generation.
    arg("reuseCheckFactoryInstance", "true")
}
```

## Download

```groovy
def reuse_version = "0.1.2"

implementation "com.github.landarskiy.reuse:reuse:$reuse_version"
//for include default adapters
implementation "com.github.landarskiy.reuse:reuse-adapter:$reuse_version"

ksp "com.github.landarskiy.reuse:reuse-compiler-ksp:$reuse_version"
```

Add KSP source set and KSP plugin in your top level `build.gradle` file

```groovy
plugins {
    id 'com.google.devtools.ksp' version ksp_version
}

subprojects { project ->
    project.afterEvaluate {
        boolean hasKspPlugin = project.plugins.hasPlugin('com.google.devtools.ksp')
        if (hasKspPlugin) {
            project.android {
                sourceSets.forEach { sourceSet ->
                    sourceSet.java.srcDirs += "${project.projectDir}/build/generated/ksp/${sourceSet.name}/kotlin"
                }
            }
        }
    }
}
```

Add KSP plugin to your module level `build.gradle` file

```groovy
plugins {
    id 'com.google.devtools.ksp'
}
```