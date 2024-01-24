<script lang="ts">
  import SideBarMainContentLayout from '$lib/components/SideBarMainContentLayout.svelte';
  import messages from '$media/messages.json';

  import { Button, Select, TextArea } from "stwui";
  import type { SelectOption } from "stwui/types";

  let selectedIssueType: String;
  
  const options: SelectOption[] = [
    {
      value: 'option_1',
      label: 'Option 1'
    },
    {
      value: 'option_2',
      label: 'Option 2',
    },
    {
      value: 'option_3',
      label: 'Option 3'
    }
  ];

  const details: SelectOption[] = [
    {
      value: 'detail_1',
      label: 'Detail 1'
    },
    {
      value: 'detail_2',
      label: 'Detail 2'
    },
    {
      value: 'detail_3',
      label: 'Detail 3'
    }
  ]


  function issueTypeChange(e: Event) {
    selectedIssueType = e.target.value;
    console.log(selectedIssueType)
  }
  
</script>


<SideBarMainContentLayout>
  <slot slot="side-bar">
    <div class="relative mx-8 my-4">{messages['reporting']['timestamp']}</div>
    <div class="relative mx-8 my-4">
      {messages['reporting']['location']}
    </div>

    <Select name="select-1" placeholder="Issue Type" on:change={issueTypeChange} {options}
      class="relative mx-8 my-4">
      <Select.Options slot="options">
        {#each options as option}
          <Select.Options.Option {option} />
        {/each}
      </Select.Options>
    </Select>

    <Select name="select-2" placeholder="Issue Details" multiple options={details}
      class="relative mx-8 my-4">
      <Select.Options slot="options">
        {#each details as detail}
          <Select.Options.Option option={detail} />
        {/each}
      </Select.Options>
    </Select>

    <TextArea name="comments" placeholder="Citizen Comments" 
      class="relative mx-8 my-4"
    />

    <div class="flex justify-between mx-8 my-4">
      <Button href="/issues/map">Back</Button>
      <Button type="primary">Submit</Button>
    </div>
    
  </slot>
  <div slot="main-content">map goes here </div>
</SideBarMainContentLayout>