# ReUse
[![latestVersion](https://jitpack.io/v/landarskiy/reuse.svg)](https://jitpack.io/#landarskiy/reuse)
![MinSdk](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)

ReUse is a helper library for RecyclerView that makes displaying different type of data through RecyclerView easily. Common idea - avoid directly using adapters and one place for creating and binding ViewHolders. You should follow next steps for makes your life less painful and easily.

### Entry

First of all you should define your entries. Entry is a communication object between ViewHolder and your data source.

```kotlin
data class TextEntry(val text: String, val style: Style) : Entry {

    override fun isSameEntry(other: Entry): Boolean {
        if (other !is TextEntry) {
            return false
        }
        return text == other.text && style == other.style
    }

    override fun isSameContent(other: Entry): Boolean {
        return true
    }

    enum class Style {
        H3, H5, H6, BODY, LIST_HEADER, LIST_CONTENT
    }
}
```

Entry interface have following methods:
- `isSameEntry(other: Entry): Boolean` - to decide whether two object represent the same Entry.
- `isSameContent(other: Entry): Boolean` - to check whether two entries have the same content.
- `getDiffPayload(other: Entry): Any?` - to get a payload about the change.

You should implement aboves methods only if you will use `DiffApater` which support DiffUtil out ob the box.

### ItemViewHolder

`ItemViewHolder` is a regular `ViewHolder` with some specific fields, methods and parameterized with your entry which used in generated code. You should create it with the same logic as usual, the onlly one difference - extend `ItemViewHolder` instead regular `RecyclerView.ViewHolder` and implement `bind()` method.

```kotlin
class TextItemViewHolder(view: View) : ItemViewHolder<TextEntry>(view) {

    private val textView: TextView = view as TextView

    override fun bind(entry: TextEntry) {
        textView.text = entry.text
    }
}
```

### RecyclerItemViewType

`RecyclerItemViewType` is a delegate which response to create specific `ItemViewHolder` for your entry object with specified view type. This approach allows to you create many UI representations for same specific entry type. You can use default implementation which use layout resource as typeId and inflate View automaticly:

```kotlin
@ViewType
class TextItemViewType : LayoutRecyclerItemViewType<TextEntry>(TYPE_ID) {

    override fun createViewHolder(
        context: Context,
        parent: ViewGroup?
    ): ItemViewHolder<CopyrightEntry> {
        return TextItemViewHolder(createView(context, parent))
    }

    companion object {
        const val TYPE_ID = R.layout.item_text
    }
}
```

Or you can use implementation without support layout resources:

```kotlin
@ViewType
class TextItemViewType : RecyclerItemViewType<TextEntry> {

    override fun createViewHolder(
        context: Context,
        parent: ViewGroup?
    ): ItemViewHolder<TextEntry> {
        return TextItemViewHolder(createView(context, parent))
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

Each `RecyclerItemViewType` should be annotated via `@ViewType` annotation. This annotation inform compiler that it should create data builder for this view type with specified entry.

You can specify scope for group some `RecyclerItemViewType` in specific data builder use `scopes` annotation parameter:

```kotlin
@ViewType(scopes = ["text_scope", "preview_scope"])
class TextItemViewType : RecyclerItemViewType<TextEntry> {
    //some implementation
}
```

### ViewTypeModule

For inform compiler about place where will be places entry point for your generated data builder you should create empty interface with `ViewTypeModule` annotation:

```kotlin
@ViewTypeModule
interface ViewTypeModule
```
After build project compiler will generate `App[InterfaceName]` object class which will contains field for each specified scope data builder. By default all your `ViewType` put into `defaultRecyclerContentFactory`. If you have custom scopes it will be grouped in `[scopeName]RecyclerContentFactory` field.

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
        dataBuilder.withTextItemViewTypeItem(TextEntry("Some text", TextEntry.Style.H3)
        //add data use generated methods
        listAdapter.setItems(dataBuilder.build())
    }
}
```

## Download

```groovy
def reuse_version = "0.0.3"

implementation "com.github.landarskiy.reuse:reuse:$reuse_version"

kapt "com.github.landarskiy.reuse:reuse-compiler:$reuse_version"
```
