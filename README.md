# ReUse
[![latestVersion](https://jitpack.io/v/landarskiy/reuse.svg)](https://jitpack.io/#landarskiy/reuse)
![MinSdk](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)

ReUse is a helper library for RecyclerView that makes displaying different type of data through RecyclerView easily. You should follow next steps for makes your life less painful and easily.

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

ItemViewHolder is a regular ViewHolder with some specific fields and methods which used in generated code. You should create it with the same logic as usual.

```kotlin
class TextItemViewHolder(view: View) : ItemViewHolder<TextEntry>(view) {

    private val textView: TextView = view as TextView

    override fun bind(entry: TextEntry) {
        textView.text = entry.text
    }
}
```

### RecyclerItemViewType

RecyclerItemViewType is a delegate which response to create specific ViewHolder for your Entry object.

```kotlin
```
