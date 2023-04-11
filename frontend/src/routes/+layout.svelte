<script>
  import colors from '$lib/colors.json';

  import Fa from 'svelte-fa/src/fa.svelte';
  import { faFacebookF, faInstagram, faTwitter } from '@fortawesome/free-brands-svg-icons';

  const primaryOne = colors["primary.one"];
  const primaryTwo = colors["primary.two"];
  const secondaryOne = colors["secondary.one"];
  const secondaryTwo = colors["secondary.two"];
  const accentOne = colors["accent.one"];
  const accentTwo = colors["accent.two"];
</script>

<slot />

<div class="footer">
  <div class="row">
    <div class="col">
      <a href="https://lomocomo.org/we-move-app/">
        <h2>Mobile App</h2>
      </a>
      <a href="https://lomocomo.org/about/contact-us/">
        <h2>Contact Us</h2>
      </a>
      
    </div>
    <div class="col">
      <a href="https://lomocomo.org/?ct_template=advocacy"><h2>Advocacy</h2></a>
      <a href="https://lomocomo.org/advocacy/livable-streets/"><li>Livable Streets</li></a>
      <a href="https://lomocomo.org/advocacy/transportation-planning/"><li>Transportation Planning</li></a>
      <a href="https://lomocomo.org/advocacy/grassroots-organizing/"><li>Grassroots Organizing</li></a>
      <a href="https://lomocomo.org/advocacy/livable-streets/"><li>Livable Streets</li></a>
      <a href="https://lomocomo.org/advocacy/missourians-for-responsible-transportation/"><li>State-Level Advocacy</li></a>

      <a href="https://lomocomo.org/consulting/"><h2>Consulting</h2></a>
    </div>
    <div class="col">
      <a href="https://lomocomo.org/events/"><h2>Events</h2></a>
      <a href="https://lomocomo.org/event/bike-walk-wheel-week/"><li>Bike, Walk, & Wheel Week</li></a>
      <a href="https://lomocomo.org/event/bikes-brews/"><li>Bikes & Brews</li></a>
      <a href="https://lomocomo.org/event/walk-audits/"><li>Walk Audits</li></a>
      <a href="https://lomocomo.org/event/lets-chat/"><li>Let's Chat!</li></a>
      <a href="https://lomocomo.org/event/velo-vini-vici/"><li>Vélo Vino Vici</li></a>

      <a href="https://lomocomo.org/programs/"><h2>Programs</h2></a>
      <a href="https://lomocomo.org/programs/bike-donations/"><li>Bike Donations</li></a>
      <a href="https://lomocomo.org/programs/giving-rides/"><li>Giving Rides</li></a>
    </div>
    <div class="col">
      <a href="https://lomocomo.org/get-involved/"><h2>Get Involved</h2></a>
      <a href="https://lomocomo.org/get-involved/membership/"><li>Membership</li></a>
      <a href="https://lomocomo.org/get-involved/volunteer/"><li>Volunteer</li></a>
      <a href="https://lomocomo.org/get-involved/sign-up-for-our-newsletter/"><li>Sign up for our newsletter</li></a>
    </div>
    <div class="col">
      <a href="https://lomocomo.org/about/"><h2>About</h2></a>
      <a href="https://lomocomo.org/about/staff/"><li>Staff</li></a>
      <a href="https://lomocomo.org/about/board-of-directors/"><li>Board of Directors</li></a>
      <a href="https://lomocomo.org/about/annual-reports"><li>Annual Reports</li></a>
      <a href="https://lomocomo.org/about/legal-and-financial/"><li>Legal and Financial</li></a>
      <a href="https://lomocomo.org/about/the-rebrand-story/"><li>Our Rebrand Story</li></a>
      <div class="socialButtons">
        <a class="socialIcon" href="https://facebook.com/localmotioncomo">
          <Fa icon={faFacebookF} size="xs"/>
        </a>
        <a class="socialIcon" href="https://facebook.com/localmotioncomo">
          <Fa icon={faInstagram} size="xs"/>
        </a>
        <a class="socialIcon" href="https://facebook.com/localmotioncomo">
          <Fa icon={faTwitter} size="xs"/>
        </a>
      </div>
    </div>
  </div>
</div>

<style>
  .col {
    display: flex; 
    flex-direction: column; 
    padding-right: 2rem;
    padding-left: 2rem;
  }

  .footer {
    background-color: #005687; 
    display: flex;
    height: 40vh;
    justify-content: space-between;
  }

  .row {
    display: flex; 
    flex-direction: row; 
    gap: 3rem;
  }

  .socialButtons {
    display: flex;
    flex-direction: row;
    padding-top: 1rem;
  }

  .socialIcon {
    align-items: center;
    background-color: #f2a900;
    border-radius: 50%;
    display: flex;
    font-size: 1.5em;
    height: 2rem;
    justify-content: center;
    margin-bottom: 0.5rem;
    margin-right: 0.5rem;
    width: 2rem;
  }

  a {
    text-decoration: none;
  }

  h2 {
    color: #F2A900;
  }

  li {
    color: white;
    list-style-type: none;
  }
  li:hover {
    color: #F2A900;
  }
</style>