import OriginalList from './List.svelte';
import OriginalItem from './Item.svelte';

const JackList = OriginalList as JackListStatic;
JackList.Item = OriginalItem as JackListItemStatic;

export { JackList };

export interface JackListStatic {
	new (...args: ConstructorParameters<typeof OriginalList>): OriginalList;
	Item: JackListItemStatic;
}

export interface JackListItemStatic {
	new (...args: ConstructorParameters<typeof OriginalItem>): OriginalItem;
}
