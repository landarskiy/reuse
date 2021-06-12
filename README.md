# ReUse
[![latestVersion](https://jitpack.io/v/landarskiy/reuse.svg)](https://jitpack.io/#landarskiy/reuse)
![MinSdk](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)

ReUse is a helper library for RecyclerView that makes displaying different type of data through RecyclerView easily. Common idea - avoid directly using adapters and one place for creating and binding ViewHolders. You should follow next steps for makes your life less painful and easily.

### BaseViewHolder

`BaseViewHolder` is a regular `ViewHolder` with some specific fields, methods and parameterized with your data class which used in generated code and for binding some information into your `ViewHolder`. You should create it with the same logic as usual, the onlly one difference - extend `BaseViewHolder` instead regular `RecyclerView.ViewHolder` and implement `bind()` method.

```kotlin
class TextViewHolder(view: View) : BaseViewHolder<TextEntry>(view) {

    private val textView: TextView = view as TextView

    override fun bind(entry: TextEntry) {
        textView.text = entry.content.text
    }
}
```

### ViewHolderFactory

`ViewHolderFactory` is a factory which response to create specific `BaseViewHolder` for your data object with specified view type. This approach allows to you create many UI representations for same specific data type. You can use default implementation which use layout resource as `typeId` and inflate View automaticly:

```kotlin
@Factory
class TextViewHolderFactory : LayoutViewHolderFactory<TextEntry>(TYPE_ID) {

    override fun createViewHolder(view: View): BaseViewHolder<TextEntry> {
        return TextViewHolder(view)
    }

    companion object {
        const val TYPE_ID = R.layout.item_text
    }
}
```

Or you can use implementation without support layout resources:

```kotlin
@Factory
class TextViewHolderFactory : ViewHolderFactory<TextEntry> {

    override fun createViewHolder(view: View): BaseViewHolder<TextEntry> {
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

Each `ViewHolderFactory` should be annotated via `@Factory` annotation. This annotation inform compiler that it should create data builder for this view type with specified data class.

You can specify scope for group some `ViewHolderFactory` in specific data builder use `scopes` annotation parameter:

```kotlin
@Factory(scopes = ["text_scope", "preview_scope"])
class TextViewHolderFactory : ViewHolderFactory<TextEntry> {
    //some implementation
}
```

### ReuseModule

For inform compiler about place where will be places entry point for your generated data builder you should create empty interface with `ReuseModule` annotation:

```kotlin
@ReuseModule
interface ReuseModule
```
After build project compiler will generate `App[InterfaceName]` object class which will contains field for each specified scope data builder. By default all your `Factory` put into `defaultRecyclerContentFactory`. If you have custom scopes it will be grouped in `[scopeName]RecyclerContentFactory` field.

### DiffEntry

In cases when you want use default implementations of adapters which support DiffUtils you can implement your data classes which you specify as a generic argument in `BaseViewHolder` and `ViewHolderFactory` as `DiffEntry` interface. 

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

*It's recommended separate your data and entry classes. `DiffEntry` usefull for transfer data from your source to ViewHolder and it's also useful using it for provide some listeners and another things which not related with your data classes but shoul be pass into ViewHolder for make some work (e.g. handle click on some UI controls).*

### DefaultRecyclerContentFactory

After you create all needed `Entry`, `ItemViewHolder` and `RecyclerItemViewType` classes you can update adapter's data use a few lines of code:

```kotlin
class MainActivity : AppCompatActivity() {
    
    private val typeFactory: DefaultRecyclerContentFactory =
        AppViewTypeModule.defaultRecyclerContentFactory
    
    private val listAdapter: DiffAdapter =
        DiffAdapter(*typeFactory.types.toTypedArray())
        
    fun updateData() {
        val dataBuilder = typeFactory.newDataBuilder()
        dataBuilder.withTextItemViewTypeItem(TextEntry(Content.Text("Some text", Content.Text.Style.H3))
        dataBuilder.withTextGroupItemViewTypeItem(TextGroupEntry(Content.GroupHeader(true)) {
            viewModel.onGroupClicked()
        })
        //add data use generated methods
        listAdapter.setItems(dataBuilder.build())
    }
}
```

## Download

```groovy
def reuse_version = "0.0.5"

implementation "com.github.landarskiy.reuse:reuse:$reuse_version"

kapt "com.github.landarskiy.reuse:reuse-compiler:$reuse_version"
```
