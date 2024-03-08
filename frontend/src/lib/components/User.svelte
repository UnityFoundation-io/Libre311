<script lang="ts">
	import { Avatar, Dropdown } from 'stwui';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import { goto } from '$app/navigation';

	const unityAuthService = useLibre311Context().unityAuthService;
	const libre311Context = useLibre311Context();
	const username = libre311Context.username;

	let isUserDropdownVisible: boolean = false;

	function toggleDropdown() {
		isUserDropdownVisible = !isUserDropdownVisible;
	}

	function logout() {
		isUserDropdownVisible = false;
		unityAuthService.logout();
		libre311Context.unsetUsername();
		goto('/');
	}
</script>

<Dropdown bind:visible={isUserDropdownVisible}>
	<button slot="trigger" on:click={toggleDropdown}>
		<Avatar initials={$username.charAt(0).toUpperCase()} />
	</button>
	<Dropdown.Items slot="items">
		<Dropdown.Items.Item on:click={logout} label="Logout"></Dropdown.Items.Item>
	</Dropdown.Items>
</Dropdown>
