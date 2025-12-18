<script lang="ts">
	import { Avatar, Dropdown } from 'stwui';
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

	function logout_keydown(e: CustomEvent) {
		const ke = e as unknown as KeyboardEvent;
		if (ke.key === 'Enter' || ke.key === ' ') {
			e.preventDefault();
			logout();
		}
	}
</script>

<Dropdown bind:visible={isUserDropdownVisible}>
	<button slot="trigger" on:click={toggleDropdown}>
		<Avatar initials={$user?.username.charAt(0).toUpperCase()} />
	</button>
	<Dropdown.Items slot="items">
		<Dropdown.Items.Item label="Logout" on:click={logout} on:keydown={logout_keydown}
		></Dropdown.Items.Item>
	</Dropdown.Items>
</Dropdown>
