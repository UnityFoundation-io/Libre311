<script lang="ts">
	import { useLibre311Context, type UserInfo } from '$lib/context/Libre311Context';
	import type { LibrePermissions } from '$lib/services/Libre311/Libre311';

	// user must have one of the permissions in the array
	export let requires: 'is-authenticated' | LibrePermissions[];

	const user = useLibre311Context().user;

	$: canView = userCanView(requires, $user);

	function userCanView(requires: 'is-authenticated' | LibrePermissions[], user: UserInfo) {
		if (!user) return false;
		if (requires == 'is-authenticated') return true;

		return user.permissions.some((p) => user.permissions.includes(p));
	}
</script>

{#if canView}
	<slot />
{/if}
