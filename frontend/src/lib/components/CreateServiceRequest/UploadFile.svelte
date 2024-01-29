<script lang="ts">
	import Breakpoint from '../Breakpoint.svelte';
	import UploadFileDesktop from './UploadFileDesktop.svelte';
	import UploadFileMobile from './UploadFileMobile.svelte';
</script>

<Breakpoint>
	<UploadFileDesktop slot="is-desktop" />
	<UploadFileMobile slot="is-mobile" />
</Breakpoint>
