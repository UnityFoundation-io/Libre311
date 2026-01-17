export const MapOrList = {
	Map: 'Map',
	List: 'List'
} as const;

export type MapOrListToggle = (typeof MapOrList)[keyof typeof MapOrList];
