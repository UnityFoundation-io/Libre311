<script lang="ts">
	import { createLibre311Context, type Libre311ContextProviderProps } from './Libre311Context';
	export let props: Libre311ContextProviderProps;

	createLibre311Context(props);
</script>

<slot />
