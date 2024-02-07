<script lang="ts">
	import { page } from '$app/stores';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import { Button, Drawer, Menu, Portal } from 'stwui';

	import { mapIcon } from './Svg/outline/mapIcon';
	import { paintBrushIcon } from './Svg/outline/paintBrushIcon';
	import { pencilIcon } from './Svg/outline/pencilIcon';
	import { tableIcon } from './Svg/outline/tableIcon';

	export let open: boolean;

	const linkResolver = useLibre311Context().linkResolver;

	function toggleDrawer() {
		open ? (open = false) : (open = true);
	}

	// Menu
	interface MenuItem {
		key: string;
		label: string;
		data: string;
		href: string;
	}

	const items: MenuItem[] = [
		{
			key: 'table',
			label: 'Service Request Table',
			data: tableIcon,
			href: linkResolver.issuesList($page.url)
		},
		{
			key: 'map',
			label: 'Request Map',
			data: mapIcon,
			href: linkResolver.issuesMap($page.url)
		}
		// TODO: Add appropriate URLs
		// {
		// 	key: 'manager',
		// 	label: 'Service Definition Manager',
		// 	data: pencilIcon,
		// 	href: '#manager'
		// },
		// {
		// 	key: 'editor',
		// 	label: 'Theme Editor',
		// 	data: paintBrushIcon,
		// 	href: '#editor'
		// }
	];
</script>

<Portal>
	{#if open}
		<Drawer handleClose={toggleDrawer} placement="left">
			<Drawer.Content slot="content">
				<Menu>
					{#each items as item}
						<Menu.Item key={item.key} label={item.label} href={item.href}>
							<Menu.Item.Icon slot="icon" data={item.data} />
						</Menu.Item>
					{/each}
				</Menu>
			</Drawer.Content>
		</Drawer>
	{/if}
</Portal>
