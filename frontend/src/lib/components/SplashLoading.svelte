<script lang='ts'>
	import messages from '$media/messages.json';
	import rocketLaunchIcon from '$lib/assets/rocket-launch.svg';
	import { Progress } from 'stwui';
</script>

<div class='splashLoad flex flex-col justify-center items-center'>
	<img class='size-16 m-2' src={rocketLaunchIcon} alt='app-logo'/>
	<h1 class='m-2 text-lg'>{messages['metadata']['app']}</h1>
	<p class='m-2 text-sm font-normal'>{messages['environment']['loading']}</p>
	<div class='m-2 max-w-52 w-52'>
		<Progress value={0} indeterminate size='sm' />
	</div>
</div>

<style>
	.splashLoad {
		height: 100vh;
		background-color: #2A2B2C;
		color: hsl(var(--primary-content));
	}
</style>
