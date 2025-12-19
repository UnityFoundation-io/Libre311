<script lang="ts">
	import { Avatar, Button, Dropdown } from 'stwui';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import { goto } from '$app/navigation';

	const unityAuthService = useLibre311Context().unityAuthService;
	const libre311Context = useLibre311Context();
	const user = libre311Context.user;

	let isUserDropdownVisible: boolean = false;

	function toggleDropdown() {
		isUserDropdownVisible = !isUserDropdownVisible;
	}

	function logout() {
		isUserDropdownVisible = false;
		unityAuthService.logout();
		goto('/');
	}
</script>

{#if $user}
	<Dropdown bind:visible={isUserDropdownVisible}>
		<button slot="trigger" aria-label="User" on:click={toggleDropdown}>
			<Avatar initials={$user?.username.charAt(0).toUpperCase()} />
		</button>
		<Dropdown.Items slot="items">
			<Dropdown.Items.Item on:click={logout} label="Logout"></Dropdown.Items.Item>
		</Dropdown.Items>
	</Dropdown>
{/if}
