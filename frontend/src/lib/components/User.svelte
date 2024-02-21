<script lang="ts">
	import { Avatar, Dropdown } from 'stwui';
	import { useLibre311Context } from '$lib/context/Libre311Context';

	const unityAuthService = useLibre311Context().unityAuthService;

	let isUserDropdownVisible: boolean = false;

	function toggleDropdown() {
		isUserDropdownVisible = !isUserDropdownVisible;
	}

	function logout() {
		isUserDropdownVisible = false;
		unityAuthService.logout();

		alert('LOGGED OUT');
	}
</script>

<Dropdown bind:visible={isUserDropdownVisible}>
	<button slot="trigger" on:click={toggleDropdown}>
		<Avatar src="broken_image.png" />
	</button>
	<Dropdown.Items slot="items">
		<Dropdown.Items.Item on:click={logout} label="Logout"></Dropdown.Items.Item>
	</Dropdown.Items>
</Dropdown>
