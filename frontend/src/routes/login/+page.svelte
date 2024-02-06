<script lang="ts">
	import Breakpoint from '$lib/components/Breakpoint.svelte';
	import LoginDesktop from '$lib/components/LoginDesktop.svelte';
	import LoginMobile from '$lib/components/LoginMobile.svelte';
</script>

<Breakpoint>
	<LoginDesktop slot="is-desktop" />
	<LoginMobile slot="is-mobile-or-tablet" />
</Breakpoint>
