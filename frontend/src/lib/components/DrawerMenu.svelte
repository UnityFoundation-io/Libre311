<script lang="ts">
	import { Drawer, Menu, Portal } from 'stwui';

	import mapSVG from '$lib/assets/map.svg';
	import paintBrushSVG from '$lib/assets/paint-brush.svg';
	import pencilSVG from '$lib/assets/pencil-square.svg';
	import tableSVG from '$lib/assets/table-cells.svg';

	export let open: boolean = false;

	function toggleDrawer() {
		open = !open;
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
			href: '#table'
		},
		{
			key: 'map',
			label: 'Request Map',
			data: mapSVG,
			href: '#map'
		},
		{
			key: 'manager',
			label: 'Service Definition Manager',
			data: pencilSVG,
			href: '#manager'
		},
		{
			key: 'editor',
			label: 'Theme Editor',
			data: paintBrushSVG,
			href: '#editor'
		}
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
