<script lang="ts">
	import SplashLoading from '$lib/components/SplashLoading.svelte';
	import Bars3 from '$lib/components/Svg/Bars3.svelte';
	import { type Libre311ContextProviderProps } from '$lib/context/Libre311Context';
	import Libre311ContextProvider from '$lib/context/Libre311ContextProvider.svelte';
	import SomethingWentWrong from '$lib/components/SomethingWentWrong.svelte';
	import '../../node_modules/leaflet-geosearch/dist/geosearch.css';
	import '../app.pcss';

	import MenuDrawer from '$lib/components/MenuDrawer.svelte';
	import { Avatar } from 'stwui';
	import { Dropdown } from 'stwui';

	import {
		asAsyncFailure,
		asAsyncSuccess,
		ASYNC_IN_PROGRESS,
		type AsyncResult
	} from '$lib/services/http';
	import { libre311Factory, type Libre311ServiceProps } from '$lib/services/Libre311/Libre311';
	import { getModeFromEnv, type Mode } from '$lib/services/mode';
	import { recaptchaServiceFactory } from '$lib/services/RecaptchaService';
	import {
		unityAuthServiceFactory,
		type UnityAuthService,
		type UnityAuthServiceProps
	} from '$lib/services/UnityAuth/UnityAuth';

	const mode: Mode = getModeFromEnv(import.meta.env);
	const recaptchaKey = String(import.meta.env.VITE_GOOGLE_RECAPTCHA_KEY);
	const unityAuthServiceProps: UnityAuthServiceProps = {
		baseURL: String(import.meta.env.VITE_AUTH_URL)
	};

	let contextProviderProps: AsyncResult<Libre311ContextProviderProps> = ASYNC_IN_PROGRESS;
	let unityAuthService: UnityAuthService;

	let open: boolean = false;

	function closeDrawer() {
		open = false;
	}

	const synchronousContextProviderProps: Omit<Libre311ContextProviderProps, 'service'> = {
		recaptchaKey,
		mode,
		unityAuthServiceProps
	};

	async function initLibre311ContextProps(serviceProps: Libre311ServiceProps) {
		try {
			const service = await libre311Factory(serviceProps);
			contextProviderProps = asAsyncSuccess({ ...synchronousContextProviderProps, service });
			unityAuthService = unityAuthServiceFactory(contextProviderProps.value.unityAuthServiceProps);
		} catch (error) {
			contextProviderProps = asAsyncFailure(error);
		}
	}

	let isUserDropdownVisible: boolean = false;

	function toggleDropdown() {
		isUserDropdownVisible = !isUserDropdownVisible;
	}

	function logout() {
		isUserDropdownVisible = false;
		unityAuthService.logout();
		alert('LOGGED OUT');
	}

	initLibre311ContextProps({
		baseURL: import.meta.env.VITE_BACKEND_URL ?? '/api',
		recaptchaService: recaptchaServiceFactory(mode, { recaptchaKey })
	});
</script>

{#if contextProviderProps.type == 'success'}
	<Libre311ContextProvider props={contextProviderProps.value} let:libre311Context>
		<header class="flex items-center justify-center">
			<div class="flex gap-4">
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

			<Dropdown bind:visible={isUserDropdownVisible}>
				<button slot="trigger" on:click={toggleDropdown}>
					<Avatar src="broken_image.png" />
				</button>
				<Dropdown.Items slot="items">
					<Dropdown.Items.Item on:click={logout} label="Logout"></Dropdown.Items.Item>
				</Dropdown.Items>
			</Dropdown>
		</header>
		<main>
			<MenuDrawer {open} handleClose={closeDrawer} />
			<slot />
		</main>
	</Libre311ContextProvider>
{:else if contextProviderProps.type == 'inProgress'}
	<SplashLoading />
{:else}
	<SomethingWentWrong />
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
	main {
		height: calc(100dvh - var(--header-height));
	}
</style>
