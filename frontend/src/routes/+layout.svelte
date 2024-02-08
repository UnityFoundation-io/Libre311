<script lang="ts">
	import SplashLoading from '$lib/components/SplashLoading.svelte';
	import Bars3 from '$lib/components/Svg/Bars3.svelte';
	import { type Libre311ContextProviderProps } from '$lib/context/Libre311Context';
	import Libre311ContextProvider from '$lib/context/Libre311ContextProvider.svelte';
	import '../../node_modules/leaflet-geosearch/dist/geosearch.css';
	import '../app.pcss';

	import MenuDrawer from '$lib/components/MenuDrawer.svelte';

	import {
		asAsyncFailure,
		asAsyncSuccess,
		ASYNC_IN_PROGRESS,
		type AsyncResult
	} from '$lib/services/http';
	import { libre311Factory, type Libre311ServiceProps } from '$lib/services/Libre311/Libre311';

	const libre311ServiceProps: Libre311ServiceProps = {
		baseURL: import.meta.env.VITE_BACKEND_URL
	};

	let contextProviderProps: AsyncResult<Libre311ContextProviderProps> = ASYNC_IN_PROGRESS;

	let open: boolean = false;

	function closeDrawer() {
		open = false;
	}

	async function createLibre311ContextProps(serviceProps: Libre311ServiceProps) {
		try {
			const service = await libre311Factory({ baseURL: serviceProps.baseURL });
			contextProviderProps = asAsyncSuccess({ service });
		} catch (error) {
			contextProviderProps = asAsyncFailure(error);
		}
	}

	createLibre311ContextProps(libre311ServiceProps);
</script>

{#if contextProviderProps.type == 'success'}
	<Libre311ContextProvider props={contextProviderProps.value} let:libre311Context>
		<header>
			<div class="controls">
				<!-- todo move inside of map -->
				<!-- <Funnel /> -->
				<button
					type="button"
					on:click={() => {
						console.log({ before: open });
						open = !open;
						console.log({ after: open });
					}}
				>
					<Bars3 />
				</button>
				<h1>{libre311Context.service.getJurisdictionConfig().jurisdiction_name}</h1>
			</div>
		</header>
		<main>
			<!-- <Drawer handleClose={closeDrawer} placement="left">
				<Drawer.Content slot="content">
					<Menu>
						<h1>Hello</h1>
					</Menu>
				</Drawer.Content>
			</Drawer> -->
			<MenuDrawer {open} handleClose={closeDrawer} />
			<slot />
		</main>
	</Libre311ContextProvider>
{:else if contextProviderProps.type == 'inProgress'}
	<!-- todo nice looking components for loading and error-->
	<SplashLoading />
{:else}
	<h1>something went wrong</h1>
{/if}

<style>
	:global(:root) {
		--header-height: 4rem;
	}
	header {
		height: var(--header-height);
		background-color: hsl(var(--primary));
		color: hsl(var(--primary-content));
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 0 1rem;
	}
	.controls {
		display: flex;
		gap: 1rem;
	}

	main {
		height: calc(100dvh - var(--header-height));
	}
</style>
