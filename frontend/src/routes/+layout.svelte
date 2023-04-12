<script>
  import FacebookSVG from '../icons/facebook.svg';
  import InstagramSVG from '../icons/instagram.svg';
  import TwitterSVG from '../icons/twitter.svg';
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
      <div class="socials">
        <a class="socialButton" href="https://facebook.com/localmotioncomo">
          <img src={FacebookSVG} alt="facebook" style="scale:4%;" />
        </a>
        <a class="socialButton" href="https://www.instagram.com/localmotioncomo/">
          <img src={InstagramSVG} alt="instagram" style="scale:2.5%;" />
        </a>
        <a class="socialButton" href="https://twitter.com/localmotioncomo">
          <img src={TwitterSVG} alt="twitter" style="scale:2.5%;" />
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
    justify-content: space-between;
  }

  .row {
    display: flex; 
    flex-direction: row;
    flex-wrap: wrap; 
    gap: 3rem;
  }

  .socials {
    display: flex;
    flex-direction: row;
    padding-top: 1rem;
  }

  .socialButton {
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