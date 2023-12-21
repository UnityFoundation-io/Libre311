<script lang="ts">
	import { createLibre311Context, type Libre311ContextProps } from './Libre311Context';
	export let props: Libre311ContextProps;
	createLibre311Context(props);
</script>

<slot />
