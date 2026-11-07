import { ActivatedRoute, Router } from '@angular/router';
import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.css'],
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: false
})
export class HomeComponent   {
  
  constructor(private route: ActivatedRoute,private http: HttpClient) { }
  email: string = ''; // Déclaration de la propriété email

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.email = params['email'];
      this.getIdCompte()
      console.log(this.IdResponse)
    });
  }
  IdResponse(IdResponse: any) {
    throw new Error('Method not implemented.');
  }
  isMenuOpened: boolean = false;

  toggleMenu(): void {
    this.isMenuOpened = !this.isMenuOpened;
  }

  clickedOutside(): void {
    this.isMenuOpened = false;
  }
  formatDateISO(date: Date): string {
    const year = date.getFullYear();
    const month = date.getMonth() + 1; // getMonth() returns 0-11
    const day = date.getDate();

    // Pad the month and day with leading zeros if necessary
    const formattedMonth = month < 10 ? `0${month}` : month;
    const formattedDay = day < 10 ? `0${day}` : day;

    return `${year}-${formattedMonth}-${formattedDay}`;
  }
  formattedDate : String ="";
  ID : number = 0
  async getIdCompte(){
    const today = new Date();
    today.setHours(0, 0, 0, 0); // Reset time to midnight
    this.formattedDate = this.formatDateISO(today);
    const getID = 'http://'+ window.location.hostname + ':8080/api/clients/'+this.email
    const IdResponse = await firstValueFrom(this.http.get<{ id: number }>(getID));
    this.ID = IdResponse.id
  }
  
}


