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

	function logoutKeydown(e: CustomEvent) {
		const ke = e as unknown as KeyboardEvent;
		if (ke.key === 'Enter' || ke.key === ' ') {
			e.preventDefault();
			logout();
		} else if (ke.key === 'Escape') {
			e.preventDefault();
			isUserDropdownVisible = false;
		}
	}

	function triggerKeydown(e: KeyboardEvent) {
		if (e.key === 'Escape' && isUserDropdownVisible) {
			e.preventDefault();
			isUserDropdownVisible = false;
		}
	}
</script>

{#if $user}
	<Dropdown bind:visible={isUserDropdownVisible}>
		<button
			slot="trigger"
			aria-label="User"
			aria-expanded={isUserDropdownVisible}
			aria-haspopup="menu"
			on:click={toggleDropdown}
			on:keydown={triggerKeydown}
            class="rounded-full focus:outline-none focus:ring-2 focus:ring-white"
		>
			<Avatar initials={$user?.username.charAt(0).toUpperCase()} />
		</button>
		<Dropdown.Items slot="items">
			<Dropdown.Items.Item label="Logout" on:click={logout} on:keydown={logoutKeydown}
			></Dropdown.Items.Item>
		</Dropdown.Items>
	</Dropdown>
{/if}
