<script lang="ts">
	import { useLibre311Context, type UserInfo } from '$lib/context/Libre311Context';
	import type { LibrePermissions } from '$lib/services/Libre311/Libre311';

	type RequiresOpts = 'is-authenticated' | 'is-anonymous' | LibrePermissions[];

	// user must have one of the permissions in the array
	export let requires: RequiresOpts;
	const user = useLibre311Context().user;

	$: canView = userCanView(requires, $user);

	function userCanView(requires: RequiresOpts, user: UserInfo) {
		if (requires == 'is-anonymous') {
			return !user;
		}
		if (requires == 'is-authenticated') {
			return Boolean(user);
		}
		if (!user) return false;

		return user.permissions.some((p) => requires.includes(p));
	}
</script>

{#if canView}
	<slot />
{/if}
