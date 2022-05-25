Change Log
==========
## Version 0.1.0

_2022-05-25_

* **API Change**: java annotation processing no longer supported. Use `com.github.landarskiy.reuse:reuse-compiler-ksp` instead
* **API Change**: `ReuseModule` annotation no longer generate class with all scopes for incremental compiling reasons
* **Rename**: `Factory` to `ReuseFactory`
* **Rename**: `BaseViewHolder` to `ReuseViewHolder`
* **Rename**: all generated scopes to `Reuse[ScopeName]ContentScope`
* **New**: added some ksp arguments for control generation process

## Version 0.0.12

_2022-01-15_

* **API Change**: scopes in generated `ReuseModule` now is factory method
* **API Change**: factories in scopes now is public

## Version 0.0.11

_2021-12-27_

* Build stabilization

## Version 0.0.10

_2021-12-26_

* **API Change**: updated `TypedDiffEntry` contract

## Version 0.0.9

_2021-12-26_

* **New**: added `TypedDiffEntry`

## Version 0.0.8

_2021-11-05_

* **New**: incremental processing

## Version 0.0.7

_2021-06-25_

* **Rename**: [_scopeName_]**RecyclerContentFactory** to [_scopeName_]**ContentScope**

## Version 0.0.6

_2021-07-08_

*  **API Change**:  adapters moved to **com.github.landarskiy.reuse:reuse-adapter:$reuse_version** dependency

## Version 0.0.5

_2021-06-12_

* **Rename**: `ViewTypeModule` to `ReuseModule`
* **Rename**: `ItemViewHolder` to `BaseViewHolder`
* **Rename**: `ViewType` to `Factory`
* **Rename**: `RecyclerItemViewType` to `ViewHolderFactory`
* **Rename**: `LayoutRecyclerItemViewType` to `LayoutViewHolderFactory`
* **Rename**: `Entry` to `DiffEntry`
* **API Change**: removed `bindData` from `BaseViewHolder`
* **API Change**: `ViewHolderFactory` became abstract class
* **API Change**: added `abstract fun createViewHolder(view: View): BaseViewHolder<T>`. Client code should implement this method instead `fun createViewHolder(context: Context, parent: ViewGroup?): BaseViewHolder<T>`
* **API Change**: `Adapter` now not work with `DiffEntry` out of the box, need use another implementation - `DiffAdapter` or `AsyncDiffAdapter`
* **New**: added `name` property for `Factory` annotation
* **New**: added `AdapterEntry` for make possible implement custom adapters
* **New**: added `AsyncDiffAdapter` - library implementation of `ListAdapter`
* **New**: added `DefaultAdapter` - library implementation with any data
