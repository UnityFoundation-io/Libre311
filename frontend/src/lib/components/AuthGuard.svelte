<script lang="ts">
	import { useLibre311Context, type UserInfo } from '$lib/context/Libre311Context';
	import type { LibrePermissions } from '$lib/services/Libre311/Libre311';

	type RequiresOpts = 'is-authenticated' | 'is-anonymous' | LibrePermissions[];

	
	interface Props {
		// user must have one of the permissions in the array
		requires: RequiresOpts;
		children?: import('svelte').Snippet;
	}

	let { requires, children }: Props = $props();
	const user = useLibre311Context().user;


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
	let canView = $derived(userCanView(requires, $user));
</script>

{#if canView}
	{@render children?.()}
{/if}
