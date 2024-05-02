<script lang="ts">
	import { Card, Layout, List, Menu } from 'stwui';

	interface sidebarItems {
		label: string;
		href: string;
	}

	const items: sidebarItems[] = [
		{
			label: 'User Details',
			href: '/user/details'
		},
		{
			label: 'Change Password',
			href: '/user/change_password'
		}
	];
</script>

<Card bordered={true} class="m4">
	<Card.Content slot="content" class="p-0 sm:p-0">
		<Layout class="h-full">
			<Layout.Header class="static z-0"><h2>User Settings</h2></Layout.Header>
			<Layout.Content>
				<Layout.Content.Sidebar class="flex min-h-[200px] items-center justify-center bg-red-300">
					Sidebar
					<Menu>
						{#each items as item}{/each}
					</Menu>
				</Layout.Content.Sidebar>
				<Layout.Content.Body class="flex min-h-[200px] items-center justify-center"
					>Body</Layout.Content.Body
				>
			</Layout.Content>
		</Layout>
	</Card.Content>
</Card>
