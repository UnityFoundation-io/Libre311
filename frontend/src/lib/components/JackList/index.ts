import OriginalList from './List.svelte';
import OriginalItem from './Item.svelte';
// import OriginalContent from './Content.svelte';
// import OriginalLeading from './Leading.svelte';
// import Avatar from './Avatar.svelte';
// import Description from './Description.svelte';
// import Extra from './Extra.svelte';
// import Icon from '../icon/Icon.svelte';
// import Title from './Title.svelte';

const JackList = OriginalList as JackListStatic;
JackList.Item = OriginalItem as JackListItemStatic;
// List.Item.Leading = OriginalLeading as ListItemLeadingStatic;
// List.Item.Leading.Avatar = Avatar;
// List.Item.Leading.Icon = Icon;
// List.Item.Content = OriginalContent as ListItemContentStatic;
// List.Item.Content.Title = Title;
// List.Item.Content.Description = Description;
// List.Item.Extra = Extra;

export { JackList };

export interface JackListStatic {
	new (...args: ConstructorParameters<typeof OriginalList>): OriginalList;
	Item: JackListItemStatic;
}

export interface JackListItemStatic {
	new (...args: ConstructorParameters<typeof OriginalItem>): OriginalItem;
	// Leading: ListItemLeadingStatic;
	// Content: ListItemContentStatic;
	// Extra: typeof Extra;
}
