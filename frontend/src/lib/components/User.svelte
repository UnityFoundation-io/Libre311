<script lang="ts">
	// import { Avatar } from 'stwui';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import { goto } from '$app/navigation';
    import * as DropdownMenu from "$lib/components/ui/dropdown-menu/index";
    import * as Avatar from "$lib/components/ui/avatar/index";
    const unityAuthService = useLibre311Context().unityAuthService;
	const libre311Context = useLibre311Context();
	const user = libre311Context.user;

	// let isUserDropdownVisible: boolean = $state(false);

	// function toggleDropdown() {
	// 	isUserDropdownVisible = !isUserDropdownVisible;
	// }

	function logout() {
		// isUserDropdownVisible = false;
		unityAuthService.logout();
		goto('/');
	}
</script>
<DropdownMenu.Root>
    <DropdownMenu.Trigger>
	{#snippet child({props})}
		<button {...props}>
<!--			<Avatar initials={$user?.username.charAt(0).toUpperCase()} />-->
            <Avatar.Root>
                <Avatar.Fallback>
            {$user?.username.charAt(0).toUpperCase()}
                </Avatar.Fallback>
            </Avatar.Root>
		</button>
	{/snippet}
    </DropdownMenu.Trigger>
		<DropdownMenu.Content >
			<DropdownMenu.Item onSelect={logout}>Logout</DropdownMenu.Item>
		</DropdownMenu.Content>
</DropdownMenu.Root>
