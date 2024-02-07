<script lang="ts">
	import { page } from '$app/stores';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import { Button, Drawer, Menu, Portal } from 'stwui';

	import mapSVG from '$lib/assets/map.svg';
	import paintBrushSVG from '$lib/assets/paint-brush.svg';
	import pencilSVG from '$lib/assets/pencil-square.svg';
	import tableSVG from '$lib/assets/table-cells.svg';

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
			data: tableSVG,
			href: linkResolver.issuesList($page.url)
		},
		{
			key: 'map',
			label: 'Request Map',
			data: mapSVG,
			href: linkResolver.issuesMap($page.url)
		}
		// TODO: Add appropriate URLs
		// {
		// 	key: 'manager',
		// 	label: 'Service Definition Manager',
		// 	data: pencilSVG,
		// 	href: '#manager'
		// },
		// {
		// 	key: 'editor',
		// 	label: 'Theme Editor',
		// 	data: paintBrushSVG,
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
