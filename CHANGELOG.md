Change Log
==========
## Version 0.0.8

_2021-11-05_

* **New**: incremental processing

## Version 0.0.7

_2021-06-25_

* **Rename**: [_scopeName_]**RecyclerContentFactory** to [_scopeName_]**ContentScope**

## Version 0.0.6

_2021-07-08_

*  **API Change**:  adpters moved to **com.github.landarskiy.reuse:reuse-adapter:$reuse_version** dependency

## Version 0.0.5

_2021-06-12_

 *  **Rename**:  `ViewTypeModule` to `ReuseModule`
 *  **Rename**:  `ItemViewHolder` to `BaseViewHolder`
 *  **Rename**:  `ViewType` to `Factory`
 *  **Rename**:  `RecyclerItemViewType` to `ViewHolderFactory`
 *  **Rename**:  `LayoutRecyclerItemViewType` to `LayoutViewHolderFactory`
 *  **Rename**:  `Entry` to `DiffEntry`
 *  **API Change**:  removed `bindData` from `BaseViewHolder`
 *  **API Change**:  `ViewHolderFactory` became abstract class
 *  **API Change**:  added `abstract fun createViewHolder(view: View): BaseViewHolder<T>`. Client code should implement this method instead `fun createViewHolder(context: Context, parent: ViewGroup?): BaseViewHolder<T>`
*  **API Change**:  `Adapter` now not work with `DiffEntry` out of the box, need use another implementation - `DiffAdapter` or `AsyncDiffAdapter`
 *  **New**: added `name` property for `Factory` annotation
 *  **New**: added `AdapterEntry` for make possible implement custom adapters
 *  **New**: added `AsyncDiffAdapter` - library implementation of `ListAdapter`
 *  **New**: added `DefaultAdapter` - library implementation with any data
