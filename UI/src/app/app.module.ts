import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';

import { AppComponent } from './app.component';
import { routing }        from './app.routing';

import { NavbarComponent } from './navbar/navbar.component';
import { FooterComponent } from './footer/footer.component';
import { LoginComponent } from './login/login.component';
import { AlertComponent } from './_components/alert/alert.component';
import { JwtInterceptor} from './_helpers/jwt.interceptor';
import { ErrorInterceptor } from "./_helpers/error.interceptor";
import { RegisterComponent } from './register/register.component';
import { HomeComponent } from './home/home.component';
import { NotfoundComponent } from './notfound/notfound.component';
import { PostsComponent } from './posts/posts.component';
import { PostDetailsComponent } from './home/post-details/post-details.component';
import { AllPostsComponent } from './home/all-posts/all-posts.component';
import { MyPostsComponent } from './home/my-posts/my-posts.component';
import { MyPostDetailsComponent } from './home/my-post-details/my-post-details.component';

@NgModule({
  declarations: [
    AppComponent,
    NavbarComponent,
    FooterComponent,
    LoginComponent,
    AlertComponent,
    RegisterComponent,
    HomeComponent,
    NotfoundComponent,
    PostsComponent,
    PostDetailsComponent,
    AllPostsComponent,
    MyPostsComponent,
    MyPostDetailsComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    routing
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true },
        { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true },

  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
