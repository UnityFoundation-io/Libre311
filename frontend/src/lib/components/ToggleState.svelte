<script lang="ts">
	export let startingValue: boolean;

	function toggle() {
		startingValue = !startingValue;
	}
</script>

<slot show={startingValue} {toggle} />
